import {
  BooleanField,
  BooleanInput,
  Create,
  Datagrid,
  DateField,
  Edit,
  List,
  NumberField,
  NumberInput,
  ReferenceField,
  ReferenceInput,
  required,
  SelectField,
  SelectInput,
  SimpleForm,
  TextField,
  TextInput,
} from 'react-admin';

const STATUS_CHOICES = [
  { id: 'ONLINE', name: 'Online' },
  { id: 'OFFLINE', name: 'Offline' },
  { id: 'CONNECTING', name: 'Connecting' },
];

const CLIENT_CHOICES = Array.from({ length: 11 }, (_, i) => ({ id: i, name: i === 0 ? 'Normal' : `Client ${i}` }));

const filters = [
  <TextInput key="q" source="q" label="Tìm (acc/char/manager)" alwaysOn />,
  <ReferenceInput key="srv" source="serverId" reference="servers">
    <SelectInput optionText="name" label="Server" />
  </ReferenceInput>,
  <SelectInput key="cli" source="client" choices={CLIENT_CHOICES} label="Client" />,
  <SelectInput key="st" source="obsStatus" choices={STATUS_CHOICES} label="Trạng thái" />,
  <BooleanInput key="en" source="enable" label="Đang bật" />,
];

export const BotList = () => (
  <List filters={filters} sort={{ field: 'id', order: 'ASC' }} perPage={25}>
    <Datagrid rowClick="edit" bulkActionButtons={false}>
      <NumberField source="id" />
      <ReferenceField source="serverId" reference="servers" link={false}>
        <TextField source="name" />
      </ReferenceField>
      <TextField source="account" />
      <TextField source="charName" label="Nhân vật" />
      <TextField source="manager" label="Quản lý" />
      <NumberField source="client" />
      <BooleanField source="enable" label="Bật" />
      <SelectField source="obsStatus" choices={STATUS_CHOICES} label="Trạng thái" />
      <NumberField source="obsLevel" label="Lvl" />
      <NumberField source="obsCoin" label="Xu" />
      <DateField source="updatedAt" showTime label="Cập nhật" />
    </Datagrid>
  </List>
);

const BotForm = (
  <SimpleForm>
    <ReferenceInput source="serverId" reference="servers">
      <SelectInput optionText="name" validate={required()} />
    </ReferenceInput>
    <TextInput source="account" validate={required()} />
    <TextInput source="password" validate={required()} />
    <TextInput source="charName" />
    <TextInput source="manager" label="Quản lý (nick)" />
    <TextInput source="chat" multiline fullWidth helperText="Chat tự động — phân cách bằng dấu ;" />
    <TextInput source="sms" multiline fullWidth helperText="Tin nhắn riêng tư — phân cách bằng dấu ;" />
    <NumberInput source="mapId" />
    <NumberInput source="zoneId" />
    <NumberInput source="posX" />
    <NumberInput source="posY" />
    <NumberInput source="playFee" label="Phí (xu)" />
    <NumberInput source="typeLuckyDraw" label="Type vòng xoay (0/1)" />
    <SelectInput source="client" choices={CLIENT_CHOICES} />
    <BooleanInput source="enable" defaultValue label="Đang bật" />
  </SimpleForm>
);

export const BotCreate = () => <Create>{BotForm}</Create>;
export const BotEdit = () => <Edit mutationMode="pessimistic">{BotForm}</Edit>;
