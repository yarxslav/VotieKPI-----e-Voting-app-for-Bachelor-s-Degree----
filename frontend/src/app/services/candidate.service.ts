import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ImageDto } from '../models/image-dto.model';
import {environment} from "../../environments/environment";

@Injectable({
  providedIn: 'root'
})
export class CandidateService {

  constructor(private http: HttpClient) {}

  getCandidateImages(): Observable<ImageDto[]> {
    return this.http.get<ImageDto[]>(`${environment.baseUrl}/candidates/images`);
  }
}
