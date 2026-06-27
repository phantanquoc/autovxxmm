import { Admin, Resource } from 'react-admin';
import BlockIcon from '@mui/icons-material/Block';
import DnsIcon from '@mui/icons-material/Dns';
import HistoryIcon from '@mui/icons-material/History';
import PeopleIcon from '@mui/icons-material/People';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import ShoppingCartIcon from '@mui/icons-material/ShoppingCart';
import { authProvider } from './authProvider';
import { dataProvider } from './dataProvider';
import { Dashboard } from './Dashboard';
import { BotCreate, BotEdit, BotList } from './resources/bots';
import { ServerCreate, ServerEdit, ServerList } from './resources/servers';
import { OrderList } from './resources/orders';
import { TradeLogList } from './resources/tradeLogs';
import { UserCreate, UserEdit, UserList } from './resources/users';
import { BlockCreate, BlockList } from './resources/blocks';

export const App = () => (
  <Admin
    title="VXMM Admin"
    authProvider={authProvider}
    dataProvider={dataProvider}
    dashboard={Dashboard}
    requireAuth
  >
    <Resource
      name="bots"
      list={BotList}
      create={BotCreate}
      edit={BotEdit}
      icon={SmartToyIcon}
      options={{ label: 'Bot' }}
    />
    <Resource
      name="orders"
      list={OrderList}
      icon={ShoppingCartIcon}
      options={{ label: 'Đơn hàng' }}
    />
    <Resource
      name="tradeLogs"
      list={TradeLogList}
      icon={HistoryIcon}
      options={{ label: 'Giao dịch xu' }}
    />
    <Resource
      name="servers"
      list={ServerList}
      create={ServerCreate}
      edit={ServerEdit}
      icon={DnsIcon}
      options={{ label: 'Máy chủ' }}
    />
    <Resource
      name="users"
      list={UserList}
      create={UserCreate}
      edit={UserEdit}
      icon={PeopleIcon}
      options={{ label: 'Người dùng' }}
    />
    <Resource
      name="blocks"
      list={BlockList}
      create={BlockCreate}
      icon={BlockIcon}
      options={{ label: 'Block list' }}
    />
  </Admin>
);
