interface Agency {
  id: number;
  name: string;
  address: string;
}

type Agencies = Array<Agency>;

export type { Agencies, Agency };
