import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-schema-page',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './schema-page.component.html',
  styleUrls: ['./schema-page.component.css']
})
export class SchemaPageComponent {
  tables = [
    {
      name: 'users',
      description: 'Stores user accounts.',
      columns: [
        { name: 'user_id', type: 'SERIAL', key: 'PK' },
        { name: 'username', type: 'VARCHAR(100)', key: '' },
        { name: 'email', type: 'VARCHAR(200)', key: '' },
        { name: 'created_at', type: 'TIMESTAMP', key: '' }
      ]
    },
    {
      name: 'tickers',
      description: 'Stores available market symbols.',
      columns: [
        { name: 'symbol', type: 'VARCHAR(10)', key: 'PK' },
        { name: 'company_name', type: 'VARCHAR(200)', key: '' },
        { name: 'sector', type: 'VARCHAR(100)', key: '' }
      ]
    },
    {
      name: 'strategies',
      description: 'Core logic for trading strategies.',
      columns: [
        { name: 'strategy_id', type: 'SERIAL', key: 'PK' },
        { name: 'user_id', type: 'INT', key: 'FK -> users(user_id)' },
        { name: 'symbol', type: 'VARCHAR(10)', key: 'FK -> tickers(symbol)' },
        { name: 'name', type: 'VARCHAR(200)', key: '' },
        { name: 'initial_budget', type: 'NUMERIC(15,2)', key: '' },
        { name: 'buy_rule', type: 'VARCHAR(500)', key: '' },
        { name: 'sell_rule', type: 'VARCHAR(500)', key: '' }
      ]
    },
    {
      name: 'simulation_runs',
      description: 'Aggregated backtest reports.',
      columns: [
        { name: 'run_id', type: 'SERIAL', key: 'PK' },
        { name: 'strategy_id', type: 'INT', key: 'FK -> strategies(strategy_id)' },
        { name: 'start_date', type: 'DATE', key: '' },
        { name: 'end_date', type: 'DATE', key: '' },
        { name: 'final_balance', type: 'NUMERIC(15,2)', key: '' },
        { name: 'total_return_pct', type: 'NUMERIC(10,4)', key: '' }
      ]
    },
    {
      name: 'trade_ledger',
      description: 'Detailed execution log for simulations.',
      columns: [
        { name: 'trade_id', type: 'SERIAL', key: 'PK' },
        { name: 'run_id', type: 'INT', key: 'FK -> simulation_runs(run_id)' },
        { name: 'trade_type', type: 'VARCHAR(10)', key: '' },
        { name: 'execution_date', type: 'DATE', key: '' },
        { name: 'price', type: 'NUMERIC(15,2)', key: '' },
        { name: 'quantity', type: 'INT', key: '' }
      ]
    }
  ];
}
