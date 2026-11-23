export type Role = 'ADMIN' | 'AGENT';

export interface User {
  id: number;
  username: string;
  fullName: string;
  role: Role;
}

export interface Session {
  token: string;
  user: User;
}
