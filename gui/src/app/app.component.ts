import {Component, OnInit} from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import {NavigationEnd, Router} from "@angular/router";
import { HostListener } from "@angular/core";
import {UsersService} from "./core/services/users.service";
import {ConfigService} from "./core/services/config.service";
import {PageConfig} from "./core/models/PageConfig";
import {User} from "./core/models/User";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {

  sideBarOpened = false;
  onPageWhereSideBarIsHihhen = false;
  loading = true;

  static supportedLangs : Array<string> = ['en', 'cs'];

  lastScreenWidth: number;

  minWidth = 768;
  sidebarMode = 'side';
  currentUrl: string;

  static sideNavHiddenOn : Array<string> = [
    '/login'
  ];

  static pageConfig: PageConfig;
  static user: User;

  logoUrl : String = '';
  appTitle : String = '';
  footerHtml : String = '<div></div>';

  user: User;
  isAdmin = false;

  constructor(
    private configService: ConfigService,
    private userService: UsersService,
    private translate: TranslateService,
    private router: Router
  ) {
    this.getScreenSize();

    let browserLang = translate.getBrowserLang();

    if (!AppComponent.supportedLangs.includes(browserLang)) {
      translate.setDefaultLang('en');
    } else {
      translate.setDefaultLang(browserLang);
    }

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
    this.userService.getUser().subscribe(user => {
      AppComponent.user = user;
      this.user = user;

      this.configService.getPageConfig().subscribe(pageConfig => {
          AppComponent.pageConfig = pageConfig;
          this.appTitle = pageConfig.headerLabel;
          this.logoUrl = pageConfig.logoUrl;
          this.footerHtml = pageConfig.footerHtml;

          this.isAdmin = AppComponent.user.isAdmin;
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
    return this.user.isAdmin;
  }

  public static getPageConfig() : PageConfig {
    return this.pageConfig;
  }

  public static getUser() : User {
    return AppComponent.user;
  }
}
