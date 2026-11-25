INSERT INTO users (username, email) VALUES 
('quant_admin', 'admin@quantledger.com'),
('algo_trader_1', 'trader1@quantledger.com'),
('researcher', 'research@quantledger.com')
ON CONFLICT (username) DO NOTHING;

INSERT INTO tickers (symbol, company_name, sector) VALUES 
('AAPL', 'Apple Inc.', 'Technology'),
('TSLA', 'Tesla Inc.', 'Automotive'),
('MSFT', 'Microsoft Corp.', 'Technology'),
('GOOGL', 'Alphabet Inc.', 'Technology'),
('AMZN', 'Amazon.com Inc.', 'Consumer Cyclical')
ON CONFLICT (symbol) DO NOTHING;

INSERT INTO strategies (user_id, symbol, name, initial_budget, buy_rule, sell_rule) VALUES 
(1, 'AAPL', 'Moving Average Crossover', 10000.00, 'Price < 50 SMA', 'Price > 50 SMA'),
(2, 'TSLA', 'RSI Momentum', 50000.00, 'RSI < 30', 'RSI > 70')
ON CONFLICT DO NOTHING;
