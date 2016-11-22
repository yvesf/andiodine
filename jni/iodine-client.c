#include <string.h>
#include <errno.h>
#include <stdlib.h>
#include <time.h>
#include <unistd.h>
#include <netdb.h>
#include <stdio.h>

#include <jni.h>

#include <sys/system_properties.h>

#include "iodine/src/common.h"
#include "iodine/src/tun.h"
#include "iodine/src/client.h"
#include "iodine/src/util.h"

#define IODINE_CLIENT_CLASS "org/xapek/andiodine/IodineClient"
#define IODINE_CLIENT_CLASS_LOG_CALLBACK "log_callback"
#define IODINE_CLIENT_CLASS_LOG_CALLBACK_SIG "(Ljava/lang/String;)V"
#define MAX_DNS_PROPERTIES 4

static int dns_fd;

static JavaVM *javaVM = 0;

JNIEXPORT jint JNI_OnLoad(JavaVM* jvm, void* reserved) {
    javaVM = jvm;

    return JNI_VERSION_1_6;
}

void android_log_callback(const char *msg_) {
    int i;
    JNIEnv *env;
    char *msg = strdup(msg_);

    if (!msg) {
        return;
    }

    (*javaVM)->GetEnv(javaVM, (void**)&env, JNI_VERSION_1_6);
    if (!env) {
        __android_log_print(ANDROID_LOG_ERROR, "iodine", "Native Debug: env == null");
        return;
    }

    if ((*javaVM)->AttachCurrentThread(javaVM, &env, 0) < 0) {
        __android_log_print(ANDROID_LOG_ERROR, "iodine", "Failed to get the environment using AttachCurrentThread()");
        return;
    }

    jclass clazz = (*env)->FindClass(env, IODINE_CLIENT_CLASS);
    if (!clazz) {
        __android_log_print(ANDROID_LOG_ERROR, "iodine", "Native Debug: clazz == null");
        return;
    }

    jmethodID log_callback = (*env)->GetStaticMethodID(env, clazz,
        IODINE_CLIENT_CLASS_LOG_CALLBACK, IODINE_CLIENT_CLASS_LOG_CALLBACK_SIG);
    if (!log_callback) {
        __android_log_print(ANDROID_LOG_ERROR, "iodine", "Native Debug: log_callback == null");
        return;
    }

    for (i = 0; i< strlen(msg); i++) {
        // not printable
        if ( ! (msg[i] >= 0x20 && msg[i] <= 0x7e)) {
            msg[i] = ' ';
        }
    }
    jstring message = (*env)->NewStringUTF(env, msg);
    if (!message) {
        __android_log_print(ANDROID_LOG_ERROR, "iodine", "Native Debug: message == null");
        return;
    }
    (*env)->CallStaticVoidMethod(env, clazz, log_callback, message);

    (*env)->DeleteLocalRef(env,message);
    free(msg);
}

JNIEXPORT jint JNICALL Java_org_xapek_andiodine_IodineClient_getDnsFd(
		JNIEnv *env, jclass klass) {
	return dns_fd;
}

JNIEXPORT jint JNICALL Java_org_xapek_andiodine_IodineClient_connect(
		JNIEnv *env, jclass klass, jstring j_nameserv_addr, jstring j_topdomain, jboolean j_raw_mode, jboolean j_lazy_mode,
		jstring j_password, jint j_request_hostname_size, jint j_response_fragment_size) {

	// XXX strdup leaks
	const char *__p_nameserv_addr = (*env)->GetStringUTFChars(env,
			j_nameserv_addr, NULL);
	char *p_nameserv_addr = strdup(__p_nameserv_addr);
	struct sockaddr_storage p_nameserv;
	int p_nameserv_len = get_addr(p_nameserv_addr, 53, AF_INET, 0, &p_nameserv);
	(*env)->ReleaseStringUTFChars(env, j_nameserv_addr, __p_nameserv_addr);

	const char *__p_topdomain = (*env)->GetStringUTFChars(env, j_topdomain,
			NULL);
	const char *p_topdomain = strdup(__p_topdomain);
	__android_log_print(ANDROID_LOG_ERROR, "iodine", "Topdomain from vm: %s", p_topdomain);

	(*env)->ReleaseStringUTFChars(env, j_topdomain, __p_topdomain);
	__android_log_print(ANDROID_LOG_ERROR, "iodine", "Topdomain from vm: %s", p_topdomain);

	const char *p_password = (*env)->GetStringUTFChars(env, j_password, NULL);
	char passwordField[33];
	memset(passwordField, 0, 33);
	strncpy(passwordField, p_password, 32);
	(*env)->ReleaseStringUTFChars(env, j_password, p_password);

	tun_config_android.request_disconnect = 0;

	int selecttimeout = 2; // original: 4
	int lazy_mode;
	int hostname_maxlen = j_request_hostname_size;
	int raw_mode;
	int autodetect_frag_size = j_response_fragment_size == 0 ? 1 : 0;
	int max_downstream_frag_size = j_response_fragment_size;

	if (j_raw_mode) {
		raw_mode = 1;
	} else {
		raw_mode = 0;
	}

	if (j_lazy_mode) {
		lazy_mode = 1;
	} else {
		lazy_mode = 0;
	}

	srand((unsigned) time(NULL));
	client_init();
	client_set_nameserver(&p_nameserv, p_nameserv_len);
	client_set_selecttimeout(selecttimeout);
	client_set_lazymode(lazy_mode);
	client_set_topdomain(p_topdomain);
	client_set_hostname_maxlen(hostname_maxlen);
	client_set_password(passwordField);

	if ((dns_fd = open_dns_from_host(NULL, 0, AF_INET, AI_PASSIVE)) == -1) {
		printf("Could not open dns socket: %s", strerror(errno));
		return 1;
	}

	if (client_handshake(dns_fd, raw_mode, autodetect_frag_size,
			max_downstream_frag_size)) {
		printf("Handshake unsuccessful: %s", strerror(errno));
		close(dns_fd);
		return 2;
	}

	if (client_get_conn() == CONN_RAW_UDP) {
		printf("Sending raw traffic directly to %s\n", client_get_raw_addr());
	}

	printf("Handshake successful, leave native code");
	return 0;
}


static int tunnel_continue_cb() {
	return ! tun_config_android.request_disconnect;
}

JNIEXPORT void JNICALL Java_org_xapek_andiodine_IodineClient_tunnelInterrupt(JNIEnv *env,
		jclass klass) {
	tun_config_android.request_disconnect = 1;
	client_stop();
}

JNIEXPORT jint JNICALL Java_org_xapek_andiodine_IodineClient_tunnel(JNIEnv *env,
		jclass klass, jint tun_fd) {

    printf("Run client_tunnel_cb");
	int retval = client_tunnel_cb(tun_fd, dns_fd, &tunnel_continue_cb);

	close(dns_fd);
	close(tun_fd);
	return retval;
}

// String IodineClient.getIp()
JNIEXPORT jstring JNICALL Java_org_xapek_andiodine_IodineClient_getIp(
		JNIEnv *env, jclass klass) {
	return (*env)->NewStringUTF(env, tun_config_android.ip);
}

// String IodineClient.getRemoteIp()
JNIEXPORT jstring JNICALL Java_org_xapek_andiodine_IodineClient_getRemoteIp(
		JNIEnv *env, jclass klass) {
	return (*env)->NewStringUTF(env, tun_config_android.remoteip);
}

// int IodineClient.getNetbits()
JNIEXPORT jint JNICALL Java_org_xapek_andiodine_IodineClient_getNetbits(
		JNIEnv *env, jclass klass) {
	return tun_config_android.netbits;
}

// int IodineClient.getMtu()
JNIEXPORT jint JNICALL Java_org_xapek_andiodine_IodineClient_getMtu(JNIEnv *env,
		jclass klass) {
	return tun_config_android.mtu;
}

// String IodineClient.getPropertyNetDns1
JNIEXPORT jstring JNICALL Java_org_xapek_andiodine_IodineClient_getPropertyNetDns1(
		JNIEnv *env, jclass klass) {
	struct sockaddr_in sa;
	for (int i = 1; i <= MAX_DNS_PROPERTIES; i++) {
		char prop_name[PROP_NAME_MAX];
		char dns[PROP_VALUE_MAX];
		snprintf(prop_name, sizeof(prop_name), "net.dns%d", i);
		__system_property_get(prop_name, dns);
		if (inet_pton(AF_INET, dns, &(sa.sin_addr)) == 1) {
			return (*env)->NewStringUTF(env, dns);
		}
	}
	return (*env)->NewStringUTF(env, "");
}
