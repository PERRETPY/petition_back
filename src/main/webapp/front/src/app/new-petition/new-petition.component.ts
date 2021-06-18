import { Component, OnInit } from '@angular/core';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {PetitionPost} from '../../models/petitionPost.model';
import {PetitionService} from '../../services/petition.service';

@Component({
  selector: 'app-new-petition',
  templateUrl: './new-petition.component.html',
  styleUrls: ['./new-petition.component.scss']
})
export class NewPetitionComponent implements OnInit {

  petitionForm: FormGroup;

  tags: string[] = [];

  maxLengthDescription = 1000;
  maxLengthTags = 3;

  moreTags = true;

  constructor(private formBuilder: FormBuilder,
              private petitionService: PetitionService) { }

  ngOnInit(): void {
    this.initForm();
    this.tags = [
      'Économie',
      'Société',
      'Politique',
      'Animaux',
      'Enfant',
      'Éducation'
    ];
  }

  initForm(): void {
    this.petitionForm = this.formBuilder.group({
      title: ['', Validators.required],
      description: ['', [Validators.required, Validators.maxLength(this.maxLengthDescription)]],
      tags: this.formBuilder.array([])
    });

  }

  onSubmitForm(): void{
    const formValue = this.petitionForm.value;
    const newPetition = new PetitionPost(
      formValue.title,
      formValue.description,
      formValue.tags ? formValue.tags : []
    );
    this.petitionService.postPetition(newPetition);
    console.log(this.petitionForm.controls['description'].value.length);
  }

  getTags(): FormArray {
    return this.petitionForm.get('tags') as FormArray;
  }

  onAddTag(): void {
    if (this.getTags().length < this.maxLengthTags) {
      this.moreTags = true;
      const newTagsControl = this.formBuilder.control('', Validators.required);
      console.log(this.tags.indexOf(newTagsControl.toString()));
      this.getTags().push(newTagsControl);
    }else {
      this.moreTags = false;
    }
    if (this.getTags().length === this.maxLengthTags) {
      this.moreTags = false;
    }
  }

  onDeleteTag(i: number): void {
    this.getTags().removeAt(i);
    this.moreTags = true;
  }

}
