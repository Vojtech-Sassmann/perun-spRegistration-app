package cz.metacentrum.perun.spRegistration.service;

import cz.metacentrum.perun.spRegistration.persistence.models.Facility;
import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttribute;
import cz.metacentrum.perun.spRegistration.persistence.models.Request;
import cz.metacentrum.perun.spRegistration.service.exceptions.CannotChangeStatusException;
import cz.metacentrum.perun.spRegistration.service.exceptions.ResourceNotFoundException;
import cz.metacentrum.perun.spRegistration.service.exceptions.UnauthorizedActionException;

import java.util.List;
import java.util.Map;

/**
 * Service layer with methods specific for Users.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public interface UserService {

	/**
	 * Create request for Registration of SP (initial version of facility, not stored in Perun yet).
	 * @param userId ID of requesting user.
	 * @param attributes Attributes set for SP (key = attribute name, value = attribute).
	 * @return Generated request ID after storing to the DB.
	 */
	Long createRegistrationRequest(Long userId, List<PerunAttribute> attributes);

	/**
	 * Create request for changes of SP (which already exists as facility in Perun).
	 * @param facilityId ID of facility in Perun.
	 * @param userId ID of requesting user.
	 * @param attributes Attributes set for SP (key = attribute name, value = attribute).
	 * @return Generated request ID after stroing to the DB.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	Long createFacilityChangesRequest(Long facilityId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException;

	/**
	 * Create request for removal of SP (which already exists as facility in Perun).
	 * @param userId ID of requesting user.
	 * @param facilityId ID of facility in Perun.
	 * @return Generated request ID after stroing to the DB.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	Long createRemovalRequest(Long userId, Long facilityId)
			throws UnauthorizedActionException;

	/**
	 * Update existing request in DB with new data.
	 * @param requestId ID of request in DB.
	 * @param userId ID of requesting user.
	 * @param attributes Attributes set for SP (key = attribute name, value = attribute).
	 * @return True if everything went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	boolean updateRequest(Long requestId, Long userId, List<PerunAttribute> attributes)
			throws UnauthorizedActionException;

	/**
	 * Ask for approval of the request from Admin.
	 * @param requestId ID of request.
	 * @param userId ID of user requesting approval.
	 * @return True if everything went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws CannotChangeStatusException when status of the request cannot be changed.
	 */
	boolean askForApproval(Long requestId, Long userId)
			throws UnauthorizedActionException, CannotChangeStatusException;

	/**
	 * Cancel pending request.
	 * @param requestId ID of request.
	 * @param userId ID of user canceling the request.
	 * @return True if everything went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws CannotChangeStatusException when status of the request cannot be changed.
	 */
	boolean cancelRequest(Long requestId, Long userId)
	throws UnauthorizedActionException, CannotChangeStatusException;

	/**
	 * Renew canceled request.
	 * @param requestId ID of request.
	 * @param userId ID of user renewing the request.
	 * @return True if everything went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 * @throws CannotChangeStatusException when status of the request cannot be changed.
	 */
	boolean renewRequest(Long requestId, Long userId)
			throws UnauthorizedActionException, CannotChangeStatusException;

	/**
	 * Ask for moving the service to the production environment.
	 * @param facilityId ID of facility in Perun.
	 * @param userId ID of requesting user.
	 * @return True if everyting went OK.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	boolean moveToProduction(Long facilityId, Long userId) throws UnauthorizedActionException; //TODO

	/**
	 * Get all facilities from Perun where user is admin (manager).
	 * @param userId ID of user.
	 * @return List of facilities.
	 */
	List<Facility> getAllFacilitiesWhereUserIsAdmin(Long userId);

	/**
	 * Get all requests user can access (is requester or admin(manager) of facility)
	 * @param userId ID of user.
	 * @return List of requests.
	 */
	List<Request> getAllRequestsUserCanAccess(Long userId);

	/**
	 * Get detailed request.
	 * @param requestId ID of request.
	 * @param userId ID of user.
	 * @return Found request.
	 * @throws ResourceNotFoundException when resource for given id does not exist
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	Request getDetailedRequest(Long requestId, Long userId) throws UnauthorizedActionException, ResourceNotFoundException;

	/**
	 * Get detailed facility.
	 * @param facilityId ID of facility.
	 * @param userId ID of user.
	 * @return Found facility.
	 * @throws UnauthorizedActionException when user is not authorized to perform this action.
	 */
	Facility getDetailedFacility(Long facilityId, Long userId) throws UnauthorizedActionException;

}
