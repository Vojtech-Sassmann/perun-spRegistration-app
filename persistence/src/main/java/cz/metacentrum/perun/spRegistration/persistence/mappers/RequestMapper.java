package cz.metacentrum.perun.spRegistration.persistence.mappers;

import cz.metacentrum.perun.spRegistration.persistence.Config;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Mapper for Request object. Maps result retrieved from DB to Request object.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public class RequestMapper implements RowMapper<Request> {

	@Autowired
	private Config config;

	public RequestMapper(Config config) {
		this.config = config;
	}

	@Override
	public Request mapRow(ResultSet resultSet, int i) throws SQLException {
		String attrsJsonStr = resultSet.getString("attributes");
		Map<String, PerunAttribute> attrs = mapAttributes(attrsJsonStr);

		Request request = new Request();
		request.setId(resultSet.getLong("id"));
		request.setFacilityId(resultSet.getLong("facility_id"));
		if (resultSet.wasNull()) {
			request.setFacilityId(null);
		}
		request.setStatus(RequestStatus.resolve(resultSet.getInt("status")));
		request.setAction(RequestAction.resolve(resultSet.getInt("action")));
		request.setReqUserId(resultSet.getLong("req_user_id"));
		request.setAttributes(attrs);
		request.setModifiedAt(resultSet.getTimestamp("modified_at"));
		request.setModifiedBy(resultSet.getLong("modified_by"));

		return request;
	}

	private Map<String, PerunAttribute> mapAttributes(String attrsJsonStr) {
		Map<String, PerunAttribute> attributes = new HashMap<>();
		JSONObject json = new JSONObject(attrsJsonStr);
		Iterator<String> keys = json.keys();
		while (keys.hasNext()) {
			String key = keys.next();
			PerunAttribute mappedAttribute = PerunAttribute.fromJsonOfDb(key, json.getJSONObject(key), config);
			attributes.put(key, mappedAttribute);
		}

		return attributes;
	}
}
