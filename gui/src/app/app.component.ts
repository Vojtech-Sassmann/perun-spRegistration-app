import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import {faDatabase, faEnvelope, faHome} from "@fortawesome/free-solid-svg-icons";
import {NavigationEnd, Router} from "@angular/router";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {

  opened = true;
  faHome = faHome;
  faRequests = faEnvelope;
  faFacilities = faDatabase;

  currentUrl: string;

  constructor(
    translate: TranslateService,
    private router: Router
  ) {
    translate.setDefaultLang('en');

    translate.use('en');
    router.events.subscribe((_: NavigationEnd) => {
      this.currentUrl = this.router.url;
      console.log(this.currentUrl);
    });
  }
}
