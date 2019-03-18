import {Component, OnInit} from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import {NavigationEnd, Router} from "@angular/router";
import { HostListener } from "@angular/core";
import {UsersService} from "./core/services/users.service";
import {ConfigService} from "./core/services/config.service";
import {PageConfig} from "./core/models/PageConfig";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  sideBarOpened = false;
  onPageWhereSideBarIsHihhen = false;
  loading = true;

  lastScreenWidth: number;

  minWidth = 768;
  sidebarMode = 'side';
  currentUrl: string;

  static sideNavHiddenOn : Array<string> = [
    '/login'
  ];

  static pageConfig: PageConfig;
  static userAdmin: boolean;

  logoUrl : String = '';
  appTitle : String = '';
  footerHtml : String = '<div></div>';

  userLoggedIn = false;
  isAdmin = false;

  constructor(
    private configService: ConfigService,
    private userService: UsersService,
    private translate: TranslateService,
    private router: Router
  ) {
    this.getScreenSize();

    translate.setDefaultLang('en');

    // TODO remove on production
    translate.use('en');
    router.events.subscribe((_: NavigationEnd) => {
      this.currentUrl = this.router.url;
    });
  }

  @HostListener('window:resize', ['$event'])
  getScreenSize(event?) {

    if (this.onPageWhereSideBarIsHihhen) {
      this.sideBarOpened = false;
      return;
    }
    
    if (window.innerWidth > this.minWidth) {
      this.sideBarOpened = true;
      this.sidebarMode = 'side';
    } else if (this.lastScreenWidth > this.minWidth) {
      this.sideBarOpened = false;
    }

    if (window.innerWidth <= this.minWidth) {
      this.sidebarMode = 'over';
    }

    this.lastScreenWidth = window.innerWidth;
  }

  closeSideBar() {
      if (this.sidebarMode == 'over') {
          this.sideBarOpened = false;
      }
  }

  ngOnInit(): void {
    this.userService.isUserAdmin().subscribe(value => {
      AppComponent.userAdmin = value;

      this.configService.getPageConfig().subscribe(pageConfig => {
          AppComponent.pageConfig = pageConfig;
          this.appTitle = pageConfig.headerLabel;
          this.logoUrl = pageConfig.logoUrl;
          this.footerHtml = pageConfig.footerHtml;

          this.isAdmin = AppComponent.userAdmin;
          this.loading = false;
      });
    });

    this.router.events.subscribe((val : NavigationEnd) => {
      if (val instanceof NavigationEnd) {
        this.onPageWhereSideBarIsHihhen = AppComponent.sideNavHiddenOn.includes(val.urlAfterRedirects);
        this.getScreenSize();
      }
    })
  }

  public static isUserAdmin() : boolean {
    return this.userAdmin;
  }

  public static getPageConfig() : PageConfig {
    return this.pageConfig;
  }
}
