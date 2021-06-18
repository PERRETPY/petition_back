import { Component, OnInit } from '@angular/core';
import {Subscription} from 'rxjs';
import {PetitionService} from '../../services/petition.service';
import {AuthenticatorService} from '../../services/authenticator.service';
import {SocialUser} from 'angularx-social-login';

@Component({
  selector: 'app-my-account',
  templateUrl: './my-account.component.html',
  styleUrls: ['./my-account.component.scss']
})
export class MyAccountComponent implements OnInit {

  signedPetitions: any[];
  signedPetitionsSubscription: Subscription;

  myPetitions: any[];
  myPetitionsSubscription: Subscription;

  user: SocialUser;
  userSubscription: Subscription;

  constructor(private petitionService: PetitionService,
              private authenticatorService: AuthenticatorService) { }

  ngOnInit(): void {
    this.signedPetitionsSubscription = this.petitionService.signedPetitionSubject.subscribe(
      (petition: any[]) => {
        this.signedPetitions = petition;
      }
    );
    this.petitionService.emitSignedPetition();
    this.petitionService.getPetitionSigned();


    this.myPetitionsSubscription = this.petitionService.myPetitionSubject.subscribe(
      (petition: any[]) => {
        this.myPetitions = petition;
      }
    );
    this.petitionService.emitMyPetition();
    this.petitionService.getMyPetition();


    this.userSubscription = this.authenticatorService.userSubject.subscribe(
      (user: SocialUser) => {
        this.user = user;
      }
    );
    this.authenticatorService.emitUserSubject();
  }

  getMyPetitions(): void {

  }

  getSignedPetitions(): void {
    this.petitionService.getPetitionSigned();
  }

}
