import {Candidate} from "./candidate.model";

export interface Voting {
  id: number | null;
  name: string;
  description: string;
  status: string;
  dateTimeFrom: string;
  dateTimeTo: string;
  category: string;
  university: string;
  group: string;
  candidates: Candidate[];
}
