import { Component, OnInit } from '@angular/core';
import {PetitionService} from '../../services/petition.service';
import {Petition} from '../../models/petition.model';
import {Subscription} from 'rxjs';

@Component({
  selector: 'app-last-petition',
  templateUrl: './last-petition.component.html',
  styleUrls: ['./last-petition.component.scss']
})
export class LastPetitionComponent implements OnInit {

  top100Petition: any[];

  top100Subscription: Subscription;

  constructor(private petitionService: PetitionService) { }

  ngOnInit(): void {
    this.top100Subscription = this.petitionService.petitionTop100Subject.subscribe(
      (petition: any[]) => {
        console.log('PETITION ON INIT : ' + petition);
        this.top100Petition = petition;
      }
    );
    this.petitionService.emitTop100Petition();
    this.petitionService.getTop100();
  }

  getNextTop100(): void {
    this.petitionService.getNextTop100();
  }

  onScroll(): void {
    console.log('scrolled!!');
    this.getNextTop100();
  }
}
