# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := iodine-client
LOCAL_LDLIBS := -lz -llog
LOCAL_CFLAGS := -Wall
LOCAL_SRC_FILES := iodine-client.c \
 iodine/src/tun.c \
 iodine/src/dns.c \
 iodine/src/read.c \
 iodine/src/encoding.c \
 iodine/src/login.c \
 iodine/src/base32.c \
 iodine/src/base64.c \
 iodine/src/base64u.c \
 iodine/src/base128.c \
 iodine/src/md5.c \
 iodine/src/common.c \
 iodine/src/client.c \
 iodine/src/util.c

include $(BUILD_SHARED_LIBRARY)
