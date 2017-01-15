import './rxjs-extensions';

import {NgModule}         from '@angular/core';
import {BrowserModule}    from '@angular/platform-browser';
import {FormsModule}      from '@angular/forms';
import {HttpModule}       from '@angular/http';

import {AppRoutingModule} from './app-routing.module';

import {AppComponent}     from './app.component';
import {IntroComponent}   from './intro.component';
import {ManualComponent}  from './manual.component';
import {HistoryComponent} from './history.component';

@NgModule({
    imports: [
        BrowserModule,
        FormsModule,
        HttpModule,
        AppRoutingModule,
    ],
    declarations: [
        AppComponent,
        IntroComponent,
        ManualComponent,
        HistoryComponent,
    ],
    bootstrap: [AppComponent],
})

export class AppModule {
}
