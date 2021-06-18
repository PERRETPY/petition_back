import { Component, OnInit } from '@angular/core';
import {PetitionService} from '../../services/petition.service';
import {Subscription} from 'rxjs';
import {Petition} from '../../models/petition.model';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  test: Petition[];
  testSubscription: Subscription;

  constructor(private petitionService: PetitionService) { }

  ngOnInit(): void {
    // this.test = this.petitionService.getPetitionSigned();
  }

}
