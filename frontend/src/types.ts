export type Role = 'ADMIN' | 'USER';
export type UserStatus = 'ACTIVE' | 'INACTIVE';

export interface UserSummary {
  id: string;
  name: string;
  cpf: string;
  birthDate: string;
  role: Role;
  profilePhotoVersion?: number;
  status: UserStatus;
}

export interface Address {
  id: string;
  zipCode: string;
  number: string;
  complement?: string;
  street: string;
  neighborhood: string;
  city: string;
  state: string;
  primary: boolean;
}

export interface UserDetails extends UserSummary {
  addresses: Address[];
  deactivatedAt?: string;
}

export interface PostalCode {
  zipCode: string;
  street: string;
  neighborhood: string;
  city: string;
  state: string;
}

export interface ApiError {
  code: string;
  message: string;
  fields?: Record<string, string>;
}
