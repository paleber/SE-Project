import { Component, OnInit } from '@angular/core';

import { Hero }        from './hero';
import { HeroService } from './hero.service';

@Component({
//  moduleId: module.id,
  selector: 'scongo-login',
  templateUrl: 'assets/app/login.component.html',
  styleUrls: [ 'assets/app/login.component.css' ],
})
export class LoginComponent implements OnInit {
  public heroes: Hero[] = [];

  constructor(private heroService: HeroService) { }

  public ngOnInit(): void {
    this.heroService.getHeroes()
      .then(heroes => this.heroes = heroes.slice(1, 5));
  }
}
