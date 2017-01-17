import {Component} from '@angular/core';

@Component({
    selector: 'scongo-app',
    templateUrl: 'assets/app/app.component.html',
    styleUrls: ['assets/app/app.component.css'],
})
export class AppComponent {
    public title = 'Game Guide (with Angular2)';

    public pages = [
        {link: 'intro', name: 'What is Scongo'},
        {link: 'manual', name: 'How To Play'},
        {link: 'history', name: 'History'}];
}
