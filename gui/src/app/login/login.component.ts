import { Component, OnInit } from '@angular/core';
import {UsersService} from "../core/services/users.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  constructor(private userService: UsersService) { }

  ngOnInit() {
  }

  login() : void{
    this.userService.login();
  }

}
