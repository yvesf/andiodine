package org.xapek.andiodine.config;

import android.content.ContentValues;

/**
 * Wrapper around ContentValues in Database
 */
public class IodineConfiguration {
	public static enum NameserverMode {
		LEAVE_DEFAULT, SET_SERVER_TUNNEL_IP, SET_CUSTOM
	}

	public static enum RequestType {
		AUTODETECT, NULL, TXT, SRV, MX, CNAME, A;

		public String getIodineName() {
			if (this == AUTODETECT) {
				return "";
			} else {
				return name();
			}
		}
	}

	private final ContentValues v;

	public IodineConfiguration() {
		v = new ContentValues();
	}

	public IodineConfiguration(ContentValues v) {
		this.v = v;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof IodineConfiguration) {
			return getId() != null && getId().equals(((IodineConfiguration) o).getId());
		} else {
			return super.equals(o);
		}
	}

	public Long getId() {
		return v.getAsLong(ConfigDatabase.COLUMN_CONF_ID);
	}

	public boolean getDefaultRoute() {
		return v.getAsInteger(ConfigDatabase.COLUMN_CONF_DEFAULT_ROUTE) == 1;
	}

	public void setDefaultRoute(boolean isDefaultRoute) {
		v.put(ConfigDatabase.COLUMN_CONF_DEFAULT_ROUTE, isDefaultRoute ? 1 : 0);
	}

	public boolean getLazyMode() {
		return v.getAsInteger(ConfigDatabase.COLUMN_CONF_LAZY_MODE) == 1;
	}

	public String getName() {
		return v.getAsString(ConfigDatabase.COLUMN_CONF_NAME);
	}

	public String getNameserver() {
		return v.getAsString(ConfigDatabase.COLUMN_CONF_NAMESERVER);
	}

	public NameserverMode getNameserverMode() {
		return NameserverMode.valueOf(v.getAsString(ConfigDatabase.COLUMN_CONF_NAMESERVER_MODE));
	}

	public String getPassword() {
		return v.getAsString(ConfigDatabase.COLUMN_CONF_PASSWORD);
	}

	public boolean getRawMode() {
		return v.getAsInteger(ConfigDatabase.COLUMN_CONF_RAW_MODE) == 1;
	}

	public RequestType getRequestType() {
		return RequestType.valueOf(v.getAsString(ConfigDatabase.COLUMN_CONF_REQUEST_TYPE));
	}

	public String getTopDomain() {
		return v.getAsString(ConfigDatabase.COLUMN_CONF_TOP_DOMAIN);
	}

	public String getTunnelNameserver() {
		return v.getAsString(ConfigDatabase.COLUMN_CONF_TUNNEL_NAMESERVER);
	}

	public void setLazyMode(boolean lazyMode) {
		v.put(ConfigDatabase.COLUMN_CONF_LAZY_MODE, lazyMode ? 1 : 0);
	}

	public void setName(String name) {
		v.put(ConfigDatabase.COLUMN_CONF_NAME, name);
	}

	public void setNameserver(String nameserver) {
		v.put(ConfigDatabase.COLUMN_CONF_NAMESERVER, nameserver);
	}

	public void setNameserverMode(NameserverMode nameserverMode) {
		v.put(ConfigDatabase.COLUMN_CONF_NAMESERVER_MODE, nameserverMode.name());
	}

	public void setPassword(String password) {
		v.put(ConfigDatabase.COLUMN_CONF_PASSWORD, password);
	}

	public void setRawMode(boolean rawMode) {
		v.put(ConfigDatabase.COLUMN_CONF_RAW_MODE, rawMode ? 1 : 0);
	}

	public void setRequestType(RequestType requestType) {
		v.put(ConfigDatabase.COLUMN_CONF_REQUEST_TYPE, requestType.name());
	}

	public void setTopDomain(String topDomain) {
		v.put(ConfigDatabase.COLUMN_CONF_TOP_DOMAIN, topDomain);
	}

	public void setTunnelNameserver(String tunnelNameserver) {
		v.put(ConfigDatabase.COLUMN_CONF_TUNNEL_NAMESERVER, tunnelNameserver);
	}

	public void setId(Long id) {
		v.put(ConfigDatabase.COLUMN_CONF_ID, id);
	}

	public ContentValues getContentValues() {
		return v;
	}

	public int getRequestHostnameSize() {
		return v.getAsInteger(ConfigDatabase.COLUMN_CONF_REQUEST_HOSTNAME_SIZE);
	}

	public void setRequestHostnameSize(int requestHostnameFragmentSize) {
		v.put(ConfigDatabase.COLUMN_CONF_REQUEST_HOSTNAME_SIZE, requestHostnameFragmentSize);
	}

	public int getResponseFragmentSize() {
		return v.getAsInteger(ConfigDatabase.COLUMN_CONF_RESPONSE_FRAGMENT_SIZE);
	}

	public void setResponseFragmentSize(int responseFragmentSize) {
		v.put(ConfigDatabase.COLUMN_CONF_RESPONSE_FRAGMENT_SIZE, responseFragmentSize);
	}

	@Override
	public String toString() {
		return "[IodineConfiguration name=" + getName() + "]";
	}
}
