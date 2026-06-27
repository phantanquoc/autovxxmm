import {
  BooleanField,
  BooleanInput,
  Create,
  Datagrid,
  DateField,
  Edit,
  List,
  required,
  SelectInput,
  SimpleForm,
  TextField,
  TextInput,
} from 'react-admin';

const ROLE_CHOICES = [
  { id: 'ADMIN', name: 'Admin' },
  { id: 'USER', name: 'User' },
];

export const UserList = () => (
  <List sort={{ field: 'id', order: 'ASC' }} perPage={50}>
    <Datagrid rowClick="edit" bulkActionButtons={false}>
      <TextField source="id" />
      <TextField source="username" />
      <TextField source="role" />
      <BooleanField source="enabled" />
      <DateField source="createdAt" showTime />
    </Datagrid>
  </List>
);

export const UserCreate = () => (
  <Create>
    <SimpleForm>
      <TextInput source="username" validate={required()} />
      <TextInput source="password" type="password" validate={required()} />
      <SelectInput source="role" choices={ROLE_CHOICES} defaultValue="USER" />
      <BooleanInput source="enabled" defaultValue />
    </SimpleForm>
  </Create>
);

export const UserEdit = () => (
  <Edit mutationMode="pessimistic">
    <SimpleForm>
      <TextInput source="username" />
      <TextInput source="password" type="password" helperText="Để trống nếu không đổi" />
      <SelectInput source="role" choices={ROLE_CHOICES} />
      <BooleanInput source="enabled" />
    </SimpleForm>
  </Edit>
);
