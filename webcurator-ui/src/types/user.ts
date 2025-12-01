interface User {
  id: number;
  firstName: string;
  lastName: string;
  name: string;
  agency: string;
  isActive: boolean;
  email: string;
  roles: string[];
}

type Users = Array<User>;

interface UsersResponse {
  filter: {
    agency: string;
  };
  amount: number;
  users: Users;
}

export type { User, Users, UsersResponse };
