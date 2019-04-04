import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {SortableTableDirective} from './layout/sortable-table.directive';
import {SortableColumnComponent} from "./layout/sortable-column.component";
import {TranslateModule} from '@ngx-translate/core';
import {PerunFooterComponent} from './layout/perun-footer/perun-footer.component';
import {FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {RouterModule} from "@angular/router";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {
  MatButtonModule,
  MatCheckboxModule, MatDialogModule, MatFormFieldModule,
  MatIconModule,
  MatInputModule, MatProgressBarModule,
  MatProgressSpinnerModule,
  MatRadioModule,
  MatSelectModule,
  MatSidenavModule,
  MatSnackBarModule,
  MatSortModule, MatStepperModule,
  MatTableModule,
  MatToolbarModule,
  MatTooltipModule,
  MatExpansionModule,
  MatChipsModule, MatMenuModule
} from "@angular/material";
import { ErrorDialogComponent } from './error-dialog/error-dialog.component';
import { NotFoundPageComponent } from './not-found-page/not-found-page.component';
import { NotAuthorizedPageComponent } from './not-authorized-page/not-authorized-page.component';
import {UserFullNamePipe} from "./pipes/user-nav-info.pipe";

@NgModule({
  imports: [
    RouterModule,
    MatProgressBarModule,
    CommonModule,
    FontAwesomeModule,
    FormsModule,
    MatSelectModule,
    MatInputModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatTooltipModule,
    MatButtonModule,
    MatToolbarModule,
    MatSnackBarModule,
    MatIconModule,
    MatTableModule,
    MatSortModule,
    TranslateModule,
    MatStepperModule,
    MatSidenavModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatExpansionModule,
    MatChipsModule,
    MatMenuModule
  ],
  declarations: [
    SortableColumnComponent,
    SortableTableDirective,
    PerunFooterComponent,
    ErrorDialogComponent,
    NotFoundPageComponent,
    NotAuthorizedPageComponent,
    UserFullNamePipe
  ],
  exports: [
    FontAwesomeModule,
    SortableColumnComponent,
    SortableTableDirective,
    TranslateModule,
    PerunFooterComponent,
    FormsModule,
    MatSelectModule,
    MatInputModule,
    MatCheckboxModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatTooltipModule,
    MatButtonModule,
    MatToolbarModule,
    MatSnackBarModule,
    MatIconModule,
    MatTableModule,
    MatSortModule,
    MatSidenavModule,
    MatStepperModule,
    MatFormFieldModule,
    ReactiveFormsModule,
    MatExpansionModule,
    ReactiveFormsModule,
    MatProgressBarModule,
    MatChipsModule,
    MatMenuModule,
    UserFullNamePipe
  ],
  entryComponents: [
    ErrorDialogComponent
  ]
})
export class SharedModule {
}
