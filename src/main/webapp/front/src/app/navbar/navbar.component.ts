import { Component, OnInit } from '@angular/core';
import {SocialAuthService, SocialUser} from 'angularx-social-login';
import {Subscription} from 'rxjs';
import {AuthenticatorService} from '../../services/authenticator.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})

export class NavbarComponent implements OnInit {

  user: SocialUser;
  userSubscription: Subscription;


  constructor(private authenticatorService: AuthenticatorService,
              private router: Router) { }

  ngOnInit(): void {
    this.userSubscription = this.authenticatorService.userSubject.subscribe(
      (user: any) => {
        this.user = user;
      }
    );
    this.authenticatorService.emitUserSubject();
    console.log(this.user.idToken);
  }


  printUser(): void {
    if (this.user != null) {
      this.authenticatorService.refreshGoogleToken();
      console.log(this.user);
    }else {
      console.log('Not connected');
    }
  }

  logOut(): void {
    this.authenticatorService.signOut();
    this.router.navigate(['/']);
  }

}
