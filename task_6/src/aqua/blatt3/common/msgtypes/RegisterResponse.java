package aqua.blatt3.common.msgtypes;

import java.io.Serializable;

@SuppressWarnings("serial")
public final class RegisterResponse implements Serializable {

	private final int leasing;
	private final String id;

	public RegisterResponse(int leasing, String id) {
		this.leasing = leasing;
		this.id = id;
	}

	public int getLeasing() {
		return leasing;
	}

	public String getId() {
		return id;
	}
}
