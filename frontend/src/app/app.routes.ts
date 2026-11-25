import { Routes } from '@angular/router';
import { SchemaPageComponent } from './components/schema-page/schema-page.component';
import { StrategiesPageComponent } from './components/strategies-page/strategies-page.component';
import { ReportsPageComponent } from './components/reports-page/reports-page.component';
import { HowToPageComponent } from './components/how-to-page/how-to-page.component';

export const routes: Routes = [
    { path: '', redirectTo: '/strategies', pathMatch: 'full' },
    { path: 'schema', component: SchemaPageComponent },
    { path: 'strategies', component: StrategiesPageComponent },
    { path: 'reports', component: ReportsPageComponent },
    { path: 'how-to', component: HowToPageComponent }
];
