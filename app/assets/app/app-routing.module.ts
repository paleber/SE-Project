import {NgModule}             from '@angular/core';
import {RouterModule, Routes} from '@angular/router';

import {IntroComponent}       from './intro.component';
import {ManualComponent}      from './manual.component';
import {HistoryComponent}     from './history.component';

const routes: Routes = [
    {path: '', redirectTo: '/intro', pathMatch: 'full'},
    {path: 'intro', component: IntroComponent},
    {path: 'manual', component: ManualComponent},
    {path: 'history', component: HistoryComponent},
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule],
})

export class AppRoutingModule {
}

