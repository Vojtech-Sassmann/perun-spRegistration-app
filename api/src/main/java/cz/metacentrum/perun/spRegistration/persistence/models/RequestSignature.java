package cz.metacentrum.perun.spRegistration.persistence.models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Approval for request of transferring service into production environment.
 *
 * @author Dominik Frantisek Bucik &lt;bucik@ics.muni.cz&gt;
 */
public class RequestSignature {

	private Long requestId;
	private Long userId;
	private LocalDateTime signedAt;
	private String name;

	public Long getRequestId() {
		return requestId;
	}

	public void setRequestId(Long requestId) {
		this.requestId = requestId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public LocalDateTime getSignedAt() {
		return signedAt;
	}

	public void setSignedAt(LocalDateTime signedAt) {
		this.signedAt = signedAt;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "RequestSignature{" +
				"requestId=" + requestId +
				", userId=" + userId +
				", signedAt=" + signedAt +
				", name='" + name + '\'' +
				'}';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RequestSignature that = (RequestSignature) o;
		return Objects.equals(requestId, that.requestId) &&
				Objects.equals(userId, that.userId) &&
				Objects.equals(signedAt, that.signedAt) &&
				Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(requestId, userId, signedAt, name);
	}
}
