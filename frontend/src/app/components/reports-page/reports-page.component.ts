import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StrategyService } from '../../services/strategy/strategy.service';
import { ReportService } from '../../services/report/report.service';
import { Strategy, SimulationRun, TradeLedger } from '../../models/types';

@Component({
  selector: 'app-reports-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports-page.component.html',
  styleUrls: ['./reports-page.component.css']
})
export class ReportsPageComponent implements OnInit {
  strategies: Strategy[] = [];
  
  // Filter state
  selectedStrategyId: number = 0;
  startDate: string = '';
  endDate: string = '';

  // Results
  currentRun: SimulationRun | null = null;
  trades: TradeLedger[] = [];
  isLoading = false;

  constructor(
    private strategyService: StrategyService,
    private reportService: ReportService
  ) {}

  ngOnInit(): void {
    // Set default dates
    const today = new Date();
    this.endDate = today.toISOString().split('T')[0];
    const lastMonth = new Date(today);
    lastMonth.setMonth(lastMonth.getMonth() - 1);
    this.startDate = lastMonth.toISOString().split('T')[0];

    this.strategyService.getAllStrategies().subscribe(data => {
      this.strategies = data;
    });
  }

  runSimulation(): void {
    if (!this.selectedStrategyId || !this.startDate || !this.endDate) {
      alert('Please fill out all filters before running a report.');
      return;
    }

    this.isLoading = true;
    this.currentRun = null;
    this.trades = [];

    this.reportService.simulateStrategy(this.selectedStrategyId, this.startDate, this.endDate)
      .subscribe({
        next: (run) => {
          this.currentRun = run;
          // After run is created, fetch its ledger
          if(run.id) {
            this.reportService.getTradesForRun(run.id).subscribe(tradesData => {
              this.trades = tradesData;
              this.isLoading = false;
            });
          } else {
             this.isLoading = false;
          }
        },
        error: (err) => {
          console.error(err);
          alert('Error generating report. Check console.');
          this.isLoading = false;
        }
      });
  }
}
