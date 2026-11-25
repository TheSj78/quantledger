import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, Ticker } from '../../models/types';

@Injectable({
  providedIn: 'root'
})
export class DropdownService {
  private apiUrl = 'http://localhost:8080/api/dropdowns';

  constructor(private http: HttpClient) { }

  getUsers(): Observable<User[]> {
    return this.http.get<User[]>(`${this.apiUrl}/users`);
  }

  getTickers(): Observable<Ticker[]> {
    return this.http.get<Ticker[]>(`${this.apiUrl}/tickers`);
  }
}
