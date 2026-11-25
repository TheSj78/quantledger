import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StrategyService } from '../../services/strategy/strategy.service';
import { DropdownService } from '../../services/dropdown/dropdown.service';
import { Strategy, User, Ticker } from '../../models/types';

@Component({
  selector: 'app-strategies-page',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './strategies-page.component.html',
  styleUrls: ['./strategies-page.component.css']
})
export class StrategiesPageComponent implements OnInit {
  strategies: Strategy[] = [];
  users: User[] = [];
  tickers: Ticker[] = [];

  // Form State
  isEditing = false;
  currentStrategy: Strategy = this.getEmptyStrategy();

  // Filter State
  filterUserId: number = 0;
  filterTicker: string = '';

  constructor(
    private strategyService: StrategyService,
    private dropdownService: DropdownService
  ) {}

  ngOnInit(): void {
    this.loadDropdownData();
    this.loadStrategies();
  }

  loadDropdownData(): void {
    this.dropdownService.getUsers().subscribe(data => this.users = data);
    this.dropdownService.getTickers().subscribe(data => this.tickers = data);
  }

  loadStrategies(): void {
    this.strategyService.getAllStrategies(this.filterUserId, this.filterTicker).subscribe(data => this.strategies = data);
  }

  applyFilters(): void {
    this.loadStrategies();
  }

  clearFilters(): void {
    this.filterUserId = 0;
    this.filterTicker = '';
    this.loadStrategies();
  }

  getEmptyStrategy(): Strategy {
    return {
      name: '',
      initialBudget: 10000,
      buyRule: '',
      sellRule: '',
      user: { id: 0, username: '', email: '' },
      ticker: { symbol: '', companyName: '', sector: '' }
    };
  }

  editStrategy(strategy: Strategy): void {
    this.isEditing = true;
    this.currentStrategy = { ...strategy };
  }

  deleteStrategy(id: number): void {
    if(confirm('Are you sure you want to delete this strategy?')) {
      this.strategyService.deleteStrategy(id).subscribe(() => {
        this.loadStrategies();
      });
    }
  }

  cancelEdit(): void {
    this.isEditing = false;
    this.currentStrategy = this.getEmptyStrategy();
  }

  saveStrategy(): void {
    // Basic validation
    if(!this.currentStrategy.name || !this.currentStrategy.user.id || !this.currentStrategy.ticker.symbol) {
      alert("Please fill in Strategy Name, User, and Ticker.");
      return;
    }

    if(this.isEditing && this.currentStrategy.id) {
      this.strategyService.updateStrategy(this.currentStrategy.id, this.currentStrategy).subscribe(() => {
        this.loadStrategies();
        this.cancelEdit();
      });
    } else {
      this.strategyService.createStrategy(this.currentStrategy).subscribe(() => {
        this.loadStrategies();
        this.cancelEdit();
      });
    }
  }
}
