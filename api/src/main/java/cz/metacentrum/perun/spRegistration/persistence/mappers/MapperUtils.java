package cz.metacentrum.perun.spRegistration.persistence.mappers;

import com.fasterxml.jackson.databind.node.NullNode;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MapperUtils class for mapping objects from RPC responses.
 *
 * @author Dominik Frantisek Bucik &lt;bucik@ics.muni.cz&gt;
 */
public class MapperUtils {

	private static final Logger log = LoggerFactory.getLogger(MapperUtils.class);

	/**
	 * Map JSON response from Perun RPC to Facility object.
	 * @param facilityJson JSON from Perun with facility.
	 * @return Mapped Facility object or null.
	 */
	public static Facility mapFacility(JSONObject facilityJson) {
		log.trace("mapFacility({})", facilityJson);
		Facility facility;

		if (facilityJson == null || facilityJson.isEmpty() || facilityJson.equals(JSONObject.NULL)) {
			facility = null;
		} else {
			facility = Facility.fromPerunJson(facilityJson);
		}

		log.trace("mapFacility() returns: {}", facility);
		return facility;
	}

	/**
	 * Map JSON response from Perun RPC to List of Facilities.
	 * @param facilitiesJson JSON from Perun with facilities.
	 * @return Mapped List of Facility objects (filled or empty) or null.
	 */
	public static List<Facility> mapFacilities(JSONArray facilitiesJson) {
		log.trace("mapFacilities({})", facilitiesJson);
		List<Facility> facilityList;

		if (facilitiesJson == null || facilitiesJson.isEmpty() || facilitiesJson.equals(JSONObject.NULL)) {
			facilityList = null;
		} else {
			facilityList = new ArrayList<>();
			for (int i = 0; i < facilitiesJson.length(); i++) {
				JSONObject facilityJson = facilitiesJson.getJSONObject(i);
				Facility facility = Facility.fromPerunJson(facilityJson);
				facilityList.add(facility);
			}
		}

		log.trace("mapFacilities() returns: {}", facilityList);
		return facilityList;
	}

	/**
	 * Map JSON response from Perun RPC to User object.
	 * @param json JSON from Perun with user.
	 * @param isRichUser Boolean value specifying if the JSON contains attributes as well.
	 * @return Mapped User object or null.
	 */
	public static User mapUser(JSONObject json, boolean isRichUser) {
		log.trace("mapUser(json: {}, isRichUser: {})", json, isRichUser);
		User user;

		if (json == null || json.isEmpty() || json.equals(JSONObject.NULL)) {
			user = null;
		} else {
			user = User.fromPerunJson(json);
			if (isRichUser) {
				JSONArray attrs = json.getJSONArray("attributes");
				for (int i = 0; i < attrs.length(); i++) {
					JSONObject attrJson = attrs.getJSONObject(i);
					if (!(attrJson.get("name") instanceof NullNode)
							&& "preferredMail".equals(attrJson.getString("name"))) {
						user.setEmail(attrJson.getString("name"));
					}
				}
			}
		}

		log.trace("mapUser() returns: {}", user);
		return user;
	}

	/**
	 * Map JSON from Perun RPC to Map of Attributes (key = attribute name, value = attribute)
	 * @param attrsJson JSON from Perun with attributes.
	 * @return Map of Attributes (filled or empty) or null.
	 */
	public static Map<String, PerunAttribute> mapAttributes(JSONArray attrsJson) {
		log.trace("mapAttributes({})", attrsJson);
		Map<String, PerunAttribute> stringPerunAttributeMap;

		if (attrsJson == null || attrsJson.isEmpty() || attrsJson.equals(JSONObject.NULL)) {
			stringPerunAttributeMap = null;
		} else {
			stringPerunAttributeMap = new HashMap<>();
			for (int i = 0; i < attrsJson.length(); i++) {
				JSONObject attrJson = attrsJson.getJSONObject(i);
				PerunAttribute a = PerunAttribute.fromJsonOfPerun(attrJson);
				if (a != null) {
					PerunAttributeDefinition def = a.getDefinition();
					a.setDefinition(def);
					stringPerunAttributeMap.put(def.getFullName(), a);
				}
			}
		}

		log.trace("mapAttributes() returns: {}", stringPerunAttributeMap);
		return stringPerunAttributeMap;
	}

	/**
	 * Map JSON from Perun RPC to PerunAttribute.
	 * @param attrJson JSON from Perun with attribute.
	 * @return Mapped PerunAttribute object or null.
	 */
	public static PerunAttribute mapAttribute(JSONObject attrJson) {
		log.trace("mapAttribute({})", attrJson);
		PerunAttribute perunAttribute;

		if (attrJson == null || attrJson.isEmpty() || attrJson.equals(JSONObject.NULL)) {
			perunAttribute = null;
		} else {
			perunAttribute = PerunAttribute.fromJsonOfPerun(attrJson);
		}

		log.trace("mapAttribute() returns: {}", perunAttribute);
		return perunAttribute;
	}

	/**
	 * Map JSON from Perun RPC to PerunAttributeDefinition
	 * @param json JSON from Perun with attribute definition.
	 * @return Mapped PerunAttributeDefinition object or null.
	 */
	public static PerunAttributeDefinition mapAttrDefinition(JSONObject json) {
		log.trace("mapAttrDefinition({})", json);
		PerunAttributeDefinition perunAttributeDefinition;

		if (json == null || json.isEmpty() || json.equals(JSONObject.NULL)) {
			perunAttributeDefinition = null;
		} else {
			perunAttributeDefinition = PerunAttributeDefinition.fromPerunJson(json);
		}

		log.trace("mapAttrDefinition() returns: {}", perunAttributeDefinition);
		return perunAttributeDefinition;
	}
}
