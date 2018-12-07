package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.persistence.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.Config;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.rpc.PerunConnector;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UserService;
import cz.metacentrum.perun.spRegistration.service.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.service.exceptions.ResourceNotFoundException;
import cz.metacentrum.perun.spRegistration.service.exceptions.UnauthorizedActionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of UserService.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Service("userService")
public class UserServiceImpl implements UserService {

	@Autowired
	private RequestManager requestManager;

	@Autowired
	private PerunConnector perunConnector;

	@Autowired
	private Config config;

	@Override
	public Long createRegistrationRequest(Long userId, List<PerunAttribute> attributes) {
		return createRequest(null, userId, RequestStatus.NEW, RequestAction.REGISTER_NEW_SP, attributes);
	}

	@Override
	public Long createFacilityChangesRequest(Long facilityId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException {
		if (! isFacilityAdmin(facilityId, userId)) {
			throw new UnauthorizedActionException("User is not registered as facility admin");
		}

		return createRequest(facilityId, userId, RequestStatus.NEW, RequestAction.UPDATE_FACILITY, attributes);
	}

	@Override
	public Long createRemovalRequest(Long userId, Long facilityId) throws UnauthorizedActionException {
		if (! isFacilityAdmin(facilityId, userId)) {
			throw new UnauthorizedActionException("User is not registered as facility admin");
		}

		return createRequest(facilityId, userId, RequestStatus.NEW, RequestAction.DELETE_FACILITY, null);
	}

	@Override
	public boolean updateRequest(Long requestId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException {
		if (! isAdminInRequest(requestId, userId)) {
			throw new UnauthorizedActionException("User is not registered as admin in request");
		}

		Request request = requestManager.getRequestByReqId(requestId);
		Map<String, PerunAttribute> convertedAttributes = ServiceUtils.transformListToMap(attributes, config);
		request.setAttributes(convertedAttributes);
		request.setModifiedBy(userId);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

		return requestManager.updateRequest(request);
	}

	@Override
	public boolean askForApproval(Long requestId, Long userId)
			throws UnauthorizedActionException, CannotChangeStatusException {
		if (! isAdminInRequest(requestId, userId)) {
			throw new UnauthorizedActionException("User is not registered as admin in request");
		}

		Request request = requestManager.getRequestByReqId(requestId);
		if (! RequestStatus.NEW.equals(request.getStatus()) ||
			! RequestStatus.WFC.equals(request.getStatus())) {
			throw new CannotChangeStatusException("Cannot ask for approval, request not marked as NEW nor WAITING_FOR_CHANGES");
		}

		request.setStatus(RequestStatus.WFA);
		request.setModifiedBy(userId);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
		requestManager.updateRequest(request);

		return true;
	}

	@Override
	public boolean cancelRequest(Long requestId, Long userId)
			throws UnauthorizedActionException, CannotChangeStatusException {
		if (! isAdminInRequest(requestId, userId)) {
			throw new UnauthorizedActionException("User is not registered as admin in request");
		}

		Request request = requestManager.getRequestByReqId(requestId);
		switch (request.getStatus()) {
			case APPROVED:
			case REJECTED:
			case CANCELED:
				throw new CannotChangeStatusException("Cannot ask for abort, request has got status "
						+ request.getStatus());
		}

		request.setStatus(RequestStatus.WFC);
		request.setModifiedBy(userId);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
		return requestManager.updateRequest(request);
	}

	@Override
	public boolean renewRequest(Long requestId, Long userId)
			throws UnauthorizedActionException, CannotChangeStatusException {
		if (! isAdminInRequest(requestId, userId)) {
			throw new UnauthorizedActionException("User is not registered as admin in request");
		}

		Request request = requestManager.getRequestByReqId(requestId);

		if (! RequestStatus.WFC.equals(request.getStatus())) {
			throw new CannotChangeStatusException("Cannot ask for renew, request not marked as WAITING_FOR_CANCEL");
		}

		request.setStatus(RequestStatus.NEW);
		request.setModifiedBy(userId);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
		return requestManager.updateRequest(request);
	}

	@Override
	public boolean moveToProduction(Long facilityId, Long userId) {
		//TODO: implement
		return false;
	}

	@Override
	public Request getDetailedRequest(Long requestId, Long userId) throws UnauthorizedActionException, ResourceNotFoundException {
		Request request = requestManager.getRequestByReqId(requestId);
		if (request == null) {
			throw new ResourceNotFoundException("Resource for given id does not exist.");
		}
		if (! isAdminInRequest(request.getReqUserId(), userId)) {
			throw new UnauthorizedActionException("User cannot view request, user is not a requester");
		}
		return requestManager.getRequestByReqId(requestId);
	}

	@Override
	public Facility getDetailedFacility(Long facilityId, Long userId) throws UnauthorizedActionException {
		if (! isFacilityAdmin(facilityId, userId)) {
			throw new UnauthorizedActionException("User cannot view facility, user is not an admin");
		}
		Map<String, PerunAttribute> attrs = perunConnector.getFacilityAttributes(facilityId);
		Facility facility = perunConnector.getFacilityById(facilityId);
		facility.setAttrs(attrs);
		return facility;
	}

	@Override
	public List<Request> getAllRequestsUserCanAccess(Long userId) {
		List<Request> requests = requestManager.getAllRequestsByUserId(userId);
		Set<Long> whereAdmin = perunConnector.getFacilityIdsWhereUserIsAdmin(userId);
		requests.addAll(requestManager.getAllRequestsByFacilityIds(whereAdmin));

		return new ArrayList<>(new HashSet<>(requests));
	}

	@Override
	public List<Facility> getAllFacilitiesWhereUserIsAdmin(Long userId) {
		List<Facility> userFacilities = perunConnector.getFacilitiesWhereUserIsAdmin(userId);
		Map<String, String> params = new HashMap<>();
		params.put(config.getIdpAttribute(), config.getIdpAttributeValue());
		List<Facility> proxyFacilities = perunConnector.getFacilitiesViaSearcher(params);

		return userFacilities.stream().filter(proxyFacilities::contains).collect(Collectors.toList());
	}

	private Long createRequest(Long facilityId, Long userId, RequestStatus status, RequestAction action, List<PerunAttribute> attributes) {
		Map<String, PerunAttribute> convertedAttributes = ServiceUtils.transformListToMap(attributes, config);

		Request request = new Request();
		request.setFacilityId(facilityId);
		request.setStatus(status);
		request.setAction(action);
		request.setAttributes(convertedAttributes);
		request.setReqUserId(userId);
		request.setModifiedBy(userId);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

		Long requestId = requestManager.createRequest(request);
		request.setId(requestId);

		return requestId;
	}

	private boolean isFacilityAdmin(Long facilityId, Long userId) {
		Set<Long> whereAdmin = perunConnector.getFacilityIdsWhereUserIsAdmin(userId);

		if (whereAdmin == null || whereAdmin.isEmpty()) {
			return false;
		}

		return whereAdmin.contains(facilityId);
	}

	private boolean isAdminInRequest(Long reqUserId, Long userId) {
		return reqUserId.equals(userId);
	}
}
