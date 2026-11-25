import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { SimulationRun, TradeLedger } from '../../models/types';

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private apiUrl = 'http://localhost:8080/api/reports';

  constructor(private http: HttpClient) { }

  simulateStrategy(strategyId: number, startDate: string, endDate: string): Observable<SimulationRun> {
    let params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate);
    return this.http.post<SimulationRun>(`${this.apiUrl}/simulate/${strategyId}`, null, { params });
  }

  getRunsForStrategy(strategyId: number): Observable<SimulationRun[]> {
    return this.http.get<SimulationRun[]>(`${this.apiUrl}/runs/${strategyId}`);
  }

  getTradesForRun(runId: number): Observable<TradeLedger[]> {
    return this.http.get<TradeLedger[]>(`${this.apiUrl}/trades/${runId}`);
  }
}
