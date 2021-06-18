import {Component, Input, OnInit} from '@angular/core';
import {Petition} from '../../models/petition.model';

@Component({
  selector: 'app-preview-petition',
  templateUrl: './preview-petition.component.html',
  styleUrls: ['./preview-petition.component.scss']
})
export class PreviewPetitionComponent implements OnInit {

  @Input() petition: Petition;

  constructor() { }

  ngOnInit(): void {
  }

}
