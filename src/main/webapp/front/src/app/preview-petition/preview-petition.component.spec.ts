import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PreviewPetitionComponent } from './preview-petition.component';

describe('PreviewPetitionComponent', () => {
  let component: PreviewPetitionComponent;
  let fixture: ComponentFixture<PreviewPetitionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PreviewPetitionComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PreviewPetitionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
