export interface User {
    id: number;
    username: string;
    email: string;
    createdAt?: string;
}

export interface Ticker {
    symbol: string;
    companyName: string;
    sector: string;
}

export interface Strategy {
    id?: number;
    user: User;
    ticker: Ticker;
    name: string;
    initialBudget: number;
    buyRule: string;
    sellRule: string;
}

export interface SimulationRun {
    id?: number;
    strategy: Strategy;
    startDate: string;
    endDate: string;
    finalBalance: number;
    totalReturnPct: number;
}

export interface TradeLedger {
    id?: number;
    simulationRun: SimulationRun;
    tradeType: string;
    executionDate: string;
    price: number;
    quantity: number;
}
