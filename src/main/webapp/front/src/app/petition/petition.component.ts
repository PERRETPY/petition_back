import {Component, Input, OnInit} from '@angular/core';
import {Petition} from '../../models/petition.model';
import {PetitionService} from '../../services/petition.service';
import {Subscription} from 'rxjs';
import {ActivatedRoute} from '@angular/router';
import {AuthenticatorService} from '../../services/authenticator.service';
import {SocialUser} from 'angularx-social-login';

@Component({
  selector: 'app-petition',
  templateUrl: './petition.component.html',
  styleUrls: ['./petition.component.scss']
})
export class PetitionComponent implements OnInit {

  @Input() idPetition: string;
  @Input() petition: Petition;

  petitionSubscription: Subscription;

  user: SocialUser;
  userSubscription: Subscription;

  constructor(private petitionService: PetitionService,
              private route: ActivatedRoute,
              private authenticatorService: AuthenticatorService) { }

  ngOnInit(): void {
    const id = this.route.snapshot.params.id;
    this.idPetition = id;
    console.log('ID Petition ' + this.idPetition);

    this.petitionSubscription = this.petitionService.currentPetitionSubject.subscribe(
      (petition: Petition) => {
        this.petition = petition;
        console.log('Petition1 : ' + petition);
      }
    );
    this.petitionService.emitCurrentPetition();
    this.petitionService.getPetitionById(this.idPetition);

    this.userSubscription = this.authenticatorService.userSubject.subscribe(
      (user: any) => {
        this.user = user;
      }
    );
    this.authenticatorService.emitUserSubject();
  }

  signPetition(): void {
    this.petitionService.signPetition(this.idPetition);
  }

}
