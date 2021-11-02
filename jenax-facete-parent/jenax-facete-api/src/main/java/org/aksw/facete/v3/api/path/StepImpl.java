package org.aksw.facete.v3.api.path;

public class StepImpl {
	protected String type;
	protected Object key;
	protected String alias;

	public StepImpl(String type, Object key, String alias) {
		super();
		this.type = type;
		this.key = key;
		this.alias = alias;
	}
	
	public String getType() {
		return type;
	}

	public Object getKey() {
		return key;
	}
	public String getAlias() {
		return alias;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StepImpl other = (StepImpl) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Step [type=" + type + ", key=" + key + ", alias=" + alias + "]";
	}
	
	
}