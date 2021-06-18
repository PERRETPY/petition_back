import { Component, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators } from '@angular/forms';
import {FacebookLoginProvider, GoogleLoginProvider, SocialAuthService, SocialUser} from 'angularx-social-login';
import {AuthenticatorService} from '../../services/authenticator.service';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-user-connexion',
  templateUrl: './user-connexion.component.html',
  styleUrls: ['./user-connexion.component.scss']
})
export class UserConnexionComponent implements OnInit {
  user: SocialUser;

  constructor(private authService: SocialAuthService,
              private authenticatorService: AuthenticatorService) { }

  ngOnInit(): void {
    this.authenticatorService.userSubject.subscribe(user => {
      this.user = user;
    });
    this.authenticatorService.emitUserSubject();
  }

  signInWithGoogle(): void {
    this.authenticatorService.signInWithGoogle();
  }

  signOut(): void {
    this.authenticatorService.signOut();
  }

}
