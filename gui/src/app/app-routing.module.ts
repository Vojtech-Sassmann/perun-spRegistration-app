import { NgModule } from '@angular/core';
import { Routes, RouterModule, PreloadAllModules } from '@angular/router';
import {MainMenuComponent} from "./main-menu/main-menu.component";
import {NotFoundPageComponent} from "./shared/not-found-page/not-found-page.component";
import {NotAuthorizedPageComponent} from "./shared/not-authorized-page/not-authorized-page.component";
import {LoginComponent} from "./login/login.component";

const routes: Routes = [
  {
    path: '',
    component: MainMenuComponent,
  },
  {
    path: 'login',
    component: LoginComponent,
  },
  {
    path: 'requests',
    loadChildren: './requests/requests.module#RequestsModule'
  },
  {
    path: 'facilities',
    loadChildren: './facilities/facilities.module#FacilitiesModule'
  },
  {
    path: 'notAuthorized',
    component: NotAuthorizedPageComponent
  },
  {
    path: '**',
    component: NotFoundPageComponent
  }

];

@NgModule({
  imports: [RouterModule.forRoot(routes, {
    preloadingStrategy: PreloadAllModules
  })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
