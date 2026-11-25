import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Strategy } from '../../models/types';

@Injectable({
  providedIn: 'root'
})
export class StrategyService {
  private apiUrl = 'http://localhost:8080/api/strategies';

  constructor(private http: HttpClient) { }

  getAllStrategies(userId?: number, ticker?: string): Observable<Strategy[]> {
    let params = new HttpParams();
    if (userId && userId > 0) params = params.set('userId', userId.toString());
    if (ticker) params = params.set('ticker', ticker);
    return this.http.get<Strategy[]>(this.apiUrl, { params });
  }

  getStrategyById(id: number): Observable<Strategy> {
    return this.http.get<Strategy>(`${this.apiUrl}/${id}`);
  }

  createStrategy(strategy: Strategy): Observable<Strategy> {
    return this.http.post<Strategy>(this.apiUrl, strategy);
  }

  updateStrategy(id: number, strategy: Strategy): Observable<Strategy> {
    return this.http.put<Strategy>(`${this.apiUrl}/${id}`, strategy);
  }

  deleteStrategy(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
