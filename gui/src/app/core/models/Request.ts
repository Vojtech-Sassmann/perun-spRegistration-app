import {RequestStatus} from "./RequestStatus";
import {RequestAction} from "./RequestAction";
import {PerunAttribute} from "./PerunAttribute";

export interface Request {
  id: number;
  facilityId: number;
  status: RequestStatus;
  action: RequestAction;
  reqUserId: number;
  attributes: Map<string, PerunAttribute>;
  modifiedAt: string;
  modifiedBy: number;
}
