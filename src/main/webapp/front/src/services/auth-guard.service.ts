import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {Observable} from 'rxjs';
import {Injectable} from '@angular/core';
import {AuthenticatorService} from './authenticator.service';

@Injectable()

export class AuthGuard implements CanActivate {

  constructor(private authenticatorService: AuthenticatorService,
              private router: Router) {
  }

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean> | Promise<boolean> | boolean  {

    if (this.authenticatorService.user != null){
      console.log('Auth OK !');
      return true;
    }else{
      console.log('Auth KO !');
      this.router.navigate(['/connection']);
    }

  }
}
