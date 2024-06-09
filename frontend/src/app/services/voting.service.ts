import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from "../../environments/environment";
import {Voting} from '../models/voting.model';

@Injectable({
  providedIn: 'root'
})

export class VotingService {
  private apiUrl = `${environment.baseUrl}/votings`;

  constructor(private http: HttpClient) {
  }

  createVoting(votingData: any): Observable<any> {
    return this.http.post(`${this.apiUrl}`, votingData);
  }

  getVotingById(id: string): Observable<Voting> {
    return this.http.get<Voting>(`${this.apiUrl}/${id}`);
  }

  updateVoting(votingId: string, votingData: any): Observable<Voting> {
    return this.http.put<Voting>(`${this.apiUrl}/${votingId}`, votingData);
  }

  deleteVoting(votingId: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${votingId}`);
  }

  searchByName(searchString: string): Observable<Voting[]> {
    return this.http.post<Voting[]>(`${this.apiUrl}/search`, {searchString});
  }

  searchByPublicId(searchString: string): Observable<Voting[]> {
    return this.http.post<Voting[]>(`${this.apiUrl}/search-adm`, {searchString});
  }
}
