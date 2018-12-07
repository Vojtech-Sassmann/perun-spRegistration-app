package cz.metacentrum.perun.spRegistration.persistence.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class represents request made by user. It contains all the data that needs to be stored.
 * It also keeps track of the modification time.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class Request {

	private Long id;
	private Long facilityId;
	private RequestStatus status;
	private RequestAction action;
	private Long reqUserId;
	private Map<String, PerunAttribute> attributes = new HashMap<>();
	private Timestamp modifiedAt;
	private Long modifiedBy;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(Long facilityId) {
		this.facilityId = facilityId;
	}

	public RequestStatus getStatus() {
		return status;
	}

	public void setStatus(RequestStatus status) {
		this.status = status;
	}

	public RequestAction getAction() {
		return action;
	}

	public void setAction(RequestAction action) {
		this.action = action;
	}

	public Long getReqUserId() {
		return reqUserId;
	}

	public void setReqUserId(Long reqUserId) {
		this.reqUserId = reqUserId;
	}

	public Map<String, PerunAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, PerunAttribute> attributes) {
		this.attributes = attributes;
	}

	public Timestamp getModifiedAt() {
		return modifiedAt;
	}

	public void setModifiedAt(Timestamp modifiedAt) {
		this.modifiedAt = modifiedAt;
	}

	public Long getModifiedBy() {
		return modifiedBy;
	}

	public void setModifiedBy(Long modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

	@JsonIgnore
	public String getFacilityName() {
		PerunAttribute attr = attributes.get("urn:perun:facility:attribute-def:def:serviceName");
		Map<String, String> value = attr.valueAsMap(false);
		return value.get("en");
	}

	@JsonIgnore
	public String getFacilityDescription() {
		PerunAttribute attr = attributes.get("urn:perun:facility:attribute-def:def:serviceDescription");
		Map<String, String> value = attr.valueAsMap(false);
		return value.get("en");
	}

	/**
	 * Convert attributes to JSON format suitable for storing into DB.
	 * @return JSON with attributes.
	 */
	@JsonIgnore
	public String getAttributesAsJsonForDb() {
		JSONObject obj = new JSONObject();
		for (Map.Entry<String ,PerunAttribute> a: attributes.entrySet()) {
			obj.put(a.getKey(), a.getValue().toJsonForDb());
		}

		return obj.toString();
	}

	/**
	 * Convert attributes to JSON format suitable for storing into Perun.
	 * @return JSON with attributes.
	 */
	@JsonIgnore
	public List<String> getAttributesAsJsonForPerun() {
		if (attributes == null || attributes.isEmpty()) {
			return null;
		}

		List<String> res = new ArrayList<>();
		for (PerunAttribute a: attributes.values()) {
			res.add(a.toJsonForPerun().toString());
		}

		return res;
	}

}
