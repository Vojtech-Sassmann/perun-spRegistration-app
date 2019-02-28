import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import {NavigationEnd, Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  opened = true;

  currentUrl: string;

  constructor(
    translate: TranslateService,
    private router: Router
  ) {
    translate.setDefaultLang('en');

    // TODO remove on production
    translate.use('en');
    router.events.subscribe((_: NavigationEnd) => {
      this.currentUrl = this.router.url;
    });
  }
}
