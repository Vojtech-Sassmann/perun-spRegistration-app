package cz.metacentrum.perun.spRegistration.service.impl;

import cz.metacentrum.perun.spRegistration.persistence.configs.AppConfig;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestAction;
import cz.metacentrum.perun.spRegistration.persistence.enums.RequestStatus;
import cz.metacentrum.perun.spRegistration.persistence.exceptions.RPCException;
import cz.metacentrum.perun.spRegistration.persistence.managers.RequestManager;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.persistence.models.User;
import cz.metacentrum.perun.spRegistration.persistence.rpc.PerunConnector;
import cz.metacentrum.perun.spRegistration.service.Mails;
import cz.metacentrum.perun.spRegistration.service.ServiceUtils;
import cz.metacentrum.perun.spRegistration.service.UserService;
import cz.metacentrum.perun.spRegistration.service.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.service.exceptions.InternalErrorException;
import cz.metacentrum.perun.spRegistration.service.exceptions.UnauthorizedActionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementation of UserService.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Service("userService")
public class UserServiceImpl implements UserService {

	private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

	private final RequestManager requestManager;
	private final PerunConnector perunConnector;
	private final AppConfig appConfig;
	private final Properties messagesProperties;

	@Autowired
	public UserServiceImpl(RequestManager requestManager, PerunConnector perunConnector, AppConfig appConfig, Properties messagesProperties) {
		this.requestManager = requestManager;
		this.perunConnector = perunConnector;
		this.appConfig = appConfig;
		this.messagesProperties = messagesProperties;
	}

	@Override
	public Long createRegistrationRequest(Long userId, List<PerunAttribute> attributes) throws InternalErrorException {
		log.debug("createRegistrationRequest(userId: {}, attributes: {})", userId, attributes);
		if (userId == null || attributes == null) {
			log.error("Illegal input - userId: {}, attributes: {}", userId, attributes);
			throw new IllegalArgumentException("Illegal input - userId: " + userId + ", attributes: " + attributes);
		}

		addProxyIdentifierAttr(attributes);

		log.debug("creating request started");
		Request req = createRequest(null, userId, attributes);
		if (req == null) {
			log.error("Could not create request");
			throw new InternalErrorException("Could not create request");
		}

		log.debug("sending mail notification");
		Mails.userCreateRequestNotify(req.getReqId(), req.getFacilityName(), req.getAdmins(appConfig.getAdminsAttr()), messagesProperties);

		log.debug("createRegistrationRequest returns: {}", req.getReqId());
		return req.getReqId();
	}

	@Override
	public Long createFacilityChangesRequest(Long facilityId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException, RPCException, InternalErrorException {
		log.debug("createFacilityChangesRequest(facility: {}, userId: {}, attributes: {})");
		if (facilityId == null || userId == null || attributes == null) {
			log.error("Illegal input - facilityId: {}, userId: {}, attributes: {}" + facilityId, userId, attributes);
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", userId: " + userId + ", attributes: " + attributes);
		} else if (! isFacilityAdmin(facilityId, userId)) {
			log.error("User is not registered as facility admin, cannot create request");
			throw new UnauthorizedActionException("User is not registered as facility admin, cannot create request");
		}

		log.debug("creating request started");
		Request req = createRequest(facilityId, userId, attributes);
		if (req == null) {
			log.error("Could not create request");
			throw new InternalErrorException("Could not create request");
		}

		log.debug("sending mail notification");
		Mails.userCreateRequestNotify(req.getReqId(), req.getFacilityName(), req.getAdmins(appConfig.getAdminsAttr()), messagesProperties);

		log.debug("createFacilityChangesRequest returns: {}", req.getReqId());
		return req.getReqId();
	}

	@Override
	public Long createRemovalRequest(Long userId, Long facilityId) throws UnauthorizedActionException, RPCException, InternalErrorException {
		log.debug("createRemovalRequest(userId: {}, facilityId: {})", userId, facilityId);
		if (facilityId == null || userId == null) {
			log.error("Illegal input - facilityId: {}, userId: {}", facilityId, userId);
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", userId: " + userId);
		} else if (! isFacilityAdmin(facilityId, userId)) {
			log.error("User is not registered as facility admin, cannot create request");
			throw new UnauthorizedActionException("User is not registered as facility admin, cannot create request");
		}

		log.debug("creating request started");
		Request req = createRequest(facilityId, userId, new ArrayList<>());
		if (req == null) {
			log.error("Could not create request");
			throw new InternalErrorException("Could not create request");
		}

		log.debug("sending mail notification");
		Mails.userCreateRequestNotify(req.getReqId(), req.getFacilityName(), req.getAdmins(appConfig.getAdminsAttr()), messagesProperties);

		log.debug("createRemovalRequest returns: {}", req.getReqId());
		return req.getReqId();
	}

	@Override
	public boolean updateRequest(Long requestId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException, InternalErrorException {
		log.debug("updateRequest(requestId: {}, userId: {}, attributes: {})", requestId, userId, attributes);
		if (requestId == null || userId == null || attributes == null) {
			log.error("Illegal input - requestId: {}, userId: {}, attributes: {}", requestId, userId, attributes);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId + ", attributes: " + attributes);
		} else if (! isAdminInRequest(requestId, userId)) {
			log.error("User is not registered as admin in request, cannot update it");
			throw new UnauthorizedActionException("User is not registered as admin in request, cannot update it");
		}

		log.debug("fetching request from DB by id: {}", requestId);
		Request request = requestManager.getRequestByReqId(requestId);
		if (request == null) {
			log.error("Could not retrieve request for id: {}", requestId);
			throw new InternalErrorException("Could not retrieve request for id: " + requestId);
		}

		log.debug("updating request stared");
		Map<String, PerunAttribute> convertedAttributes = ServiceUtils.transformListToMap(attributes, appConfig);
		request.setAttributes(convertedAttributes);
		request.setModifiedBy(userId);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
		boolean result = requestManager.updateRequest(request);
		log.debug("updated request: {}", result);

		log.debug("updateRequest returns: {}", result);
		return result;
	}

	@Override
	public boolean askForApproval(Long requestId, Long userId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException {
		if (requestId == null || userId == null) {
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId);
		} else if (! isAdminInRequest(requestId, userId)) {
			throw new UnauthorizedActionException("User is not registered as admin in request, cannot ask for approval");
		}

		Request request = requestManager.getRequestByReqId(requestId);
		if (request == null) {
			log.error("Could not retrieve request for id: {}", requestId);
			throw new InternalErrorException("Could not retrieve request for id: " + requestId);
		} else if (! RequestStatus.NEW.equals(request.getStatus()) ||
			! RequestStatus.WFC.equals(request.getStatus())) {
			throw new CannotChangeStatusException("Cannot ask for approval, request not marked as NEW nor WAITING_FOR_CHANGES");
		}

		request.setStatus(RequestStatus.WFA);
		request.setModifiedBy(userId);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
		boolean res = requestManager.updateRequest(request);

		Mails.requestStatusUpdateUserNotify(requestId, RequestStatus.WFA,
				request.getAdmins(appConfig.getAdminsAttr()), messagesProperties);
		Mails.requestApprovalAdminNotify(userId, requestId, messagesProperties);

		return res;
	}

	@Override
	public boolean cancelRequest(Long requestId, Long userId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException {
		log.debug("cancelRequest(requestId: {}, userId: {})", requestId, userId);
		if (requestId == null || userId == null) {
			log.error("Illegal input - requestId: {}, userId: {}", requestId, userId);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId);
		} else if (! isAdminInRequest(requestId, userId)) {
			log.error("User is not registered as admin in request, cannot cancel it");
			throw new UnauthorizedActionException("User is not registered as admin in request, cannot cancel it");
		}

		log.debug("fetching request from DB with id: {}", requestId);
		Request request = requestManager.getRequestByReqId(requestId);
		if (request == null) {
			log.error("Could not retrieve request for id: {}", requestId);
			throw new InternalErrorException("Could not retrieve request for id: " + requestId);
		}

		switch (request.getStatus()) {
			case APPROVED:
			case REJECTED:
			case CANCELED: {
				log.error("Cannot ask for abort, request has got status: {}", request.getStatus());
				throw new CannotChangeStatusException("Cannot ask for abort, request has got status "
						+ request.getStatus());
			}
		}

		log.debug("updating request started");
		request.setStatus(RequestStatus.WFC);
		request.setModifiedBy(userId);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
		boolean res = requestManager.updateRequest(request);
		log.debug("updating request: {}", res);

		log.debug("sending mail notification");
		Mails.requestStatusUpdateUserNotify(requestId, RequestStatus.CANCELED,
				request.getAdmins(appConfig.getAdminsAttr()), messagesProperties);

		log.debug("cancelRequest returns: {}", res);
		return res;
	}

	@Override
	public boolean renewRequest(Long requestId, Long userId)
			throws UnauthorizedActionException, CannotChangeStatusException, InternalErrorException {
		log.debug("renewRequest(requestId: {}, userId: {})", requestId, userId);
		if (requestId == null || userId == null) {
			log.error("Illegal input - requestId: {}, userId: {}", requestId, userId);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId);
		} else if (! isAdminInRequest(requestId, userId)) {
			log.error("User is not registered as admin in request, cannot renew it");
			throw new UnauthorizedActionException("User is not registered as admin in request, cannot renew it");
		}

		log.debug("fetching request from DB with id: {}", requestId);
		Request request = requestManager.getRequestByReqId(requestId);
		if (request == null) {
			log.error("Could not retrieve request for id: {}", requestId);
			throw new InternalErrorException("Could not retrieve request for id: " + requestId);
		} else if (! RequestStatus.WFC.equals(request.getStatus())) {
			log.error("Cannot ask for renew, request not marked as WAITING_FOR_CANCEL");
			throw new CannotChangeStatusException("Cannot ask for renew, request not marked as WAITING_FOR_CANCEL");
		}

		log.debug("updating request started");
		request.setStatus(RequestStatus.NEW);
		request.setModifiedBy(userId);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));
		boolean res = requestManager.updateRequest(request);
		log.debug("updating request: {}", res);

		log.debug("sending mail notification");
		Mails.requestStatusUpdateUserNotify(requestId, RequestStatus.NEW,
				request.getAdmins(appConfig.getAdminsAttr()), messagesProperties);

		log.debug("renewRequest returns:  {}", res);
		return res;
	}

	@Override
	public Long moveToProduction(Long facilityId, Long userId, List<String> authorities)
			throws UnauthorizedActionException, InternalErrorException, RPCException {
		log.debug("moveToProduction(facilityId: {}, userId: {}, authorities: {})", facilityId, userId, authorities);
		if (facilityId == null || userId == null) {
			log.error("Illegal input - facilityId: {}, userId: {}", facilityId, userId);
			throw new IllegalArgumentException("Illegal input - facilityId: " + facilityId + ", userId: " + userId);
		} else if (! isFacilityAdmin(facilityId, userId)) {
			log.error("User is not registered as admin for facility, cannot ask for moving to production");
			throw new UnauthorizedActionException("User is not registered as admin for facility, cannot ask for moving to production");
		}

		log.debug("fetching facility from Perun with id: {} for user: {}", facilityId, userId);
		Facility fac = getDetailedFacility(facilityId, userId);
		if (fac == null) {
			log.error("Could not retrieve facility for id: {}", facilityId);
			throw new InternalErrorException("Could not retrieve facility for id: " + facilityId);
		}

		log.debug("setting attributes");
		Map<String, PerunAttribute> attributes = fac.getAttrs();
		String listAttrName = appConfig.getShowOnServicesListAttribute();
		String testSpAttrName = appConfig.getTestSpAttribute();

		PerunAttribute listAttr = attributes.get(listAttrName);
		listAttr.setOldValue(listAttr.getValue());
		listAttr.setValue(true);

		PerunAttribute testSpAttr = attributes.get(testSpAttrName);
		listAttr.setOldValue(testSpAttr.getValue());
		listAttr.setValue(false);

		log.debug("creating request started");
		Request req = createRequest(facilityId, userId, RequestAction.MOVE_TO_PRODUCTION, fac.getAttrs());
		if (req == null) {
			log.error("Could not create request");
			throw new InternalErrorException("Could not create request");
		}
		log.debug("creating request finished - created request with id: {}", req.getReqId());

		log.debug("sending mail notifications");
		Mails.transferToProductionUserNotify(req.getReqId(), req.getFacilityName(), req.getAdmins(appConfig.getAdminsAttr()), messagesProperties);
		//TODO: generate approval link
		Mails.authoritiesApproveProductionTransferNotify(null, req.getFacilityName(), authorities, messagesProperties);

		log.debug("moveToProduction returns: {}", req.getReqId());
		return req.getReqId();
	}

	@Override
	public boolean signTransferToProduction(Long requestId, Long userId, String approvalName) throws InternalErrorException, RPCException {
		log.debug("signTransferToProduction(requestId: {}, userId: {}, approvalName: {})");
		if (requestId == null || userId == null) {
			log.error("Illegal input - requestId: {}, userId: {}", requestId, userId);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId);
		}

		log.debug("fetching request from DB with id: {}", requestId);
		Request request = requestManager.getRequestByReqId(requestId);
		if (request == null) {
			log.debug("Could not retrieve request for id: {}", requestId);
			throw new InternalErrorException("Could not retrieve request for id: " + requestId);
		}

		log.debug("fetching user from Perun with id: {}", userId);
		User signer = perunConnector.getRichUser(userId);
		if (signer == null) {
			log.error("Could not find user in Perun for id: {}", userId);
			throw new InternalErrorException("Could not find user in Perun for id: " + userId);
		}

		log.debug("adding signature for request with id: {} by user: {}", requestId, userId);
		boolean result = requestManager.addSignature(requestId, userId, signer.getFullName(), approvalName);
		log.debug("added signature: {}", result);

		log.debug("signTransferToProduction returns: {}", result);
		return result;
	}

	@Override
	public Request getDetailedRequest(Long requestId, Long userId)
			throws UnauthorizedActionException, InternalErrorException {
		log.debug("getDetailedRequest(requestId: {}, userId: {})", requestId, userId);

		Request request = requestManager.getRequestByReqId(requestId);
		log.debug("fetching request from DB with id: {}", requestId);
		if (request == null) {
			log.error("Could not retrieve request for id: {}", requestId);
			throw new InternalErrorException("Could not retrieve request for id: " + requestId);
		}

		if (requestId == null || userId == null) {
			log.error("Illegal input - requestId: {}, userId: {}", requestId, userId);
			throw new IllegalArgumentException("Illegal input - requestId: " + requestId + ", userId: " + userId);
		} else if (!isAdminInRequest(request.getReqUserId(), userId)) {
			log.error("User cannot view request, user is not a requester");
			throw new UnauthorizedActionException("User cannot view request, user is not a requester");
		}


		log.debug("getDetailedRequest returns: {}", request);
		return request;
	}

	@Override
	public Facility getDetailedFacility(Long facilityId, Long userId)
			throws UnauthorizedActionException, RPCException, InternalErrorException {
		log.debug("getDetailedFacility(facilityId: {}, userId: {})", facilityId, userId);
		if (! isFacilityAdmin(facilityId, userId)) {
			log.error("User cannot view facility, user is not an admin");
			throw new UnauthorizedActionException("User cannot view facility, user is not an admin");
		}

		log.debug("fetching facility from Perun with id: {}", facilityId);
		Facility facility = perunConnector.getFacilityById(facilityId);
		if (facility == null) {
			log.error("Could not retrieve facility for id: {}", facilityId);
			throw new InternalErrorException("Could not retrieve facility for id: " + facilityId);
		}

		log.debug("fetching facility attributes");
		Map<String, PerunAttribute> attrs = perunConnector.getFacilityAttributes(facilityId);
		facility.setAttrs(attrs);

		log.debug("getDetailedFacility returns: {}", facility);
		return facility;
	}

	@Override
	public List<Request> getAllRequestsUserCanAccess(Long userId) throws RPCException {
		log.debug("getAllRequestsUserCanAccess({})", userId);
		if (userId == null) {
			log.error("userId is null");
			throw new IllegalArgumentException("userId is null");
		}
		log.debug("fetching requests for user with id: {} from DB", userId);
		List<Request> requests = requestManager.getAllRequestsByUserId(userId);
		log.debug("fetching facilities where user with id: {} is admin from Perun", userId);
		Set<Long> whereAdmin = perunConnector.getFacilityIdsWhereUserIsAdmin(userId);
		requests.addAll(requestManager.getAllRequestsByFacilityIds(whereAdmin));

		log.debug("getAllRequestsUserCanAccess returns: {}", requests);
		return new ArrayList<>(new HashSet<>(requests));
	}

	@Override
	public List<Facility> getAllFacilitiesWhereUserIsAdmin(Long userId) throws RPCException {
		log.debug("getAllFacilitiesWhereUserIsAdmin({})", userId);
		if (userId == null) {
			log.error("userId is null");
			throw new IllegalArgumentException("userId is null");
		}
		log.debug("fetching facilities where user with id: {} is admin from Perun", userId);
		List<Facility> userFacilities = perunConnector.getFacilitiesWhereUserIsAdmin(userId);
		Map<String, String> params = new HashMap<>();
		params.put(appConfig.getIdpAttribute(), appConfig.getIdpAttributeValue());
		log.debug("fetching facilities by proxyIdp identifier");
		List<Facility> proxyFacilities = perunConnector.getFacilitiesViaSearcher(params);

		List<Facility> result = userFacilities.stream().filter(proxyFacilities::contains).collect(Collectors.toList());
		log.debug("getAllFacilitiesWhereUserIsAdmin returns: {}", result);
		return result;
	}

	private Request createRequest(Long facilityId, Long userId, List<PerunAttribute> attributes) throws InternalErrorException {
		Map<String, PerunAttribute> convertedAttributes = ServiceUtils.transformListToMap(attributes, appConfig);
		return createRequest(facilityId, userId, RequestAction.REGISTER_NEW_SP, convertedAttributes);
	}

	private Request createRequest(Long facilityId, Long userId, RequestAction action, Map<String,PerunAttribute> attributes) throws InternalErrorException {
		log.debug("createRequest(facilityId: {}, userId: {}, action: {}, attributes: {})", facilityId, userId, action, attributes);
		Request request = new Request();
		request.setFacilityId(facilityId);
		request.setStatus(RequestStatus.NEW);
		request.setAction(action);
		request.setAttributes(attributes);
		request.setReqUserId(userId);
		request.setModifiedBy(userId);
		request.setModifiedAt(new Timestamp(System.currentTimeMillis()));

		log.debug("creating request in DB");
		Long requestId = requestManager.createRequest(request);
		if (requestId == null) {
			log.error("Could not create request: {} in DB", request);
			throw new InternalErrorException("Could not create request in DB");

		}

		request.setReqId(requestId);

		log.debug("createRequest returns: {}", request);
		return request;
	}

	private boolean isFacilityAdmin(Long facilityId, Long userId) throws RPCException {
		log.debug("isFacilityAdmin(facilityId: {}, userId: {})", facilityId, userId);
		log.debug("fetching facilities where user with id: {} is admin from Perun", userId);
		Set<Long> whereAdmin = perunConnector.getFacilityIdsWhereUserIsAdmin(userId);

		if (whereAdmin == null || whereAdmin.isEmpty()) {
			log.debug("isFacilityAdmin returns: {}", false);
			return false;
		}

		boolean result = whereAdmin.contains(facilityId);
		log.debug("isFacilityAdmin returns:  {}", result);
		return result;
	}

	private boolean isAdminInRequest(Long reqUserId, Long userId) {
		log.debug("isAdminInRequest(reqUserId: {}, userId: {})", reqUserId, userId);

		boolean res = reqUserId.equals(userId);
		log.debug("isAdminInRequest returns: {}", res);
		return res;
	}

	private void addProxyIdentifierAttr(List<PerunAttribute> attributes) {
		String identifierAttrName = appConfig.getIdpAttribute();
		String value = appConfig.getIdpAttributeValue();

		PerunAttribute identifierAttr = new PerunAttribute();
		identifierAttr.setFullName(identifierAttrName);
		identifierAttr.setValue(Collections.singletonList(value));

		attributes.add(identifierAttr);
	}
}