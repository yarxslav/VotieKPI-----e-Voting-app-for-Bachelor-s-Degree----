import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {VoteResult} from '../models/vote-result.model';
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class VoteResultService {
  private apiUrl = `${environment.baseUrl}/vote-results`;

  constructor(private http: HttpClient) {
  }

  createVoteResult(voteResult: VoteResult): Observable<void> {
    return this.http.post<void>(this.apiUrl, voteResult);
  }

  hasUserVoted(userId: number, votingId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/${userId}/${votingId}`);
  }

  getShowVoteResults(votingId: string): Observable<any[]> {
    return this.http.get<any[]>(`${this.apiUrl}/show/${votingId}`);
  }
}
