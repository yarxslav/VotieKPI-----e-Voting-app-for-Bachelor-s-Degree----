import {Role} from "./role.model";
import {VerificationData} from "./verification-data.model";

export interface User {
  id: number;
  verificationData: VerificationData;
  voterPublicId: string;
  username: string;
  email: string;
  password: string;
  name: string;
  surname: string;
  patronymic: string;
  university: string;
  faculty: string;
  group: string;
  phone: string;
  inProgress: boolean;
  roles: Role[];
}
